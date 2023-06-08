package io.nais

import io.nais.BigQueryDatasetPermission.READWRITE
import io.nais.DatabaseType.POSTGRES_13
import io.nais.Environment.DEV
import io.nais.Environment.PROD
import io.nais.PLATFORM.*
import kotlinx.serialization.ExperimentalSerializationApi
import java.net.URL
import java.time.LocalDateTime
import java.util.*

private const val dollar = '$' // workaround, escaping doesn't work in multiline strings (https://youtrack.jetbrains.com/issue/KT-2425)

typealias RequestHandler = (Request) -> Map<String, String>

@ExperimentalSerializationApi
val serve: RequestHandler = { request ->
   mapOf(
      ".nais/nais.yaml" to naisApplicationFrom(request).serialize(),
      ".nais/dev.yaml" to appVarsFrom(request, DEV).serialize(),
      ".nais/prod.yaml" to appVarsFrom(request, PROD).serialize(),
      ".nais/alerts-dev.yaml" to alertsFrom(request).serialize(),
      ".nais/alerts-prod.yaml" to alertsFrom(request).serialize(),
      ".github/workflows/main.yaml" to gitHubWorkflowFrom(request).serialize(),
      "Dockerfile" to dockerfileFrom(request)
   ) + kafkaTopicsFrom(request).map { topic ->
      ".nais/topic-${topic.metadata.name}.yaml" to topic.serialize()
   }
}

@ExperimentalSerializationApi
fun Map<String, String>.b64EncodeValues() = mapValues { it.value.base64Encode() }

/**
 * Stolen from: https://stackoverflow.com/a/63013893/1503549
 *
 * Creates a new read-only map by replacing or adding entries to this map from another [map].
 *
 * The returned map preserves the entry iteration order of the original map.
 * Those entries of another [map] that are missing in this map are iterated in the end in the order of that [map].
 */
operator fun <K, V> Map<out K, V>.plus(map: Map<out K, V>): Map<K, V> =
   LinkedHashMap(this).apply { putAll(map) }

private val generationAnnotations = mapOf(
   "start.nais.io/created-by" to "me",

   // TODO(x10an14): Ideally replace with git commit sha:
   "start.nais.io/creationTimestamp" to LocalDateTime.now().toString(),
)

@ExperimentalSerializationApi
internal fun naisApplicationFrom(req: Request) = NaisApplication(
   apiVersion = "nais.io/v1alpha1",
   kind = "Application",
   metadata = AppMetadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team),
      annotations = generationLabels
   ),
   spec = AppSpec(
      image = "##REPLACE_IMAGE##",
      liveness = StatusEndpoint(path = "/isalive", port = 8080, initialDelay = 20, timeout = 60),
      readiness = StatusEndpoint(path = "/isready", port = 8080, initialDelay = 20, timeout = 60),
      replicas = Replicas(min = 2, max = 2, cpuThresholdPercentage = 50),
      prometheus = Prometheus(enabled = true, path = "/metrics"),
      resources = Resources(
         limits = ResourceMetrics(cpu = "200m", memory = "256Mi"),
         requests = ResourceMetrics(cpu = "200m", memory = "256Mi"),
      ),
      ingresses = "##REPLACE_INGRESS##"
   ).apply {
      req.extras.forEach { feature ->
         when (feature) {
            "idporten" -> idPorten = IdPorten(true)
            "openSearch" -> openSearch = OpenSearch(instance = req.appName)
            "aad" -> azure = Azure(application = AzureApplication(enabled = true))
         }
      }
      if (req.containsGcpSpecificThings()) gcp = gcpStuffFrom(req)
   }
)

@ExperimentalSerializationApi
internal fun appVarsFrom(req: Request, env: Environment) = Vars(
   ingresses = listOf(URL("https://${req.appName}.intern${if (env == DEV) ".dev" else ""}.nav.no")),
   kafkaPool = if (req.kafkaTopics.isNotEmpty()) "nais-${env.name.lowercase()}" else null
)

internal fun gitHubWorkflowFrom(req: Request) = GitHubWorkflow(
   name = "Build and deploy ${req.appName}",
   jobs = mapOf(
      "build" to Job(name = "build", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep) + buildStepsFor(req.platform)),
      "deployAppToDev" to Job(name = "Deploy app to dev", needs = "build", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep, appDeployStep(DEV))),
      "deployAppToProd" to Job(name = "Deploy app to prod", needs = "deployAppToDev", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep, appDeployStep(PROD))),
      "deployAlertsToDev" to Job(name = "Deploy alerts to dev", needs = "build", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep, alertDeployStep(DEV))),
      "deployAlertsToProd" to Job(name = "Deploy alerts to prod", needs = "build", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep, alertDeployStep(PROD)))
   ) + req.kafkaTopics.flatMap { topicName ->
      listOf(
         "deployTopic${topicName.replaceFirstChar { it.titlecase() }}Dev" to Job(name = "Deploy Kafka topic $topicName to dev", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep, topicDeployStep(topicName, DEV))),
         "deployTopic${topicName.replaceFirstChar { it.titlecase() }}Prod" to Job(name = "Deploy Kafka topic $topicName to prod", runsOn = "ubuntu-20.04", steps = listOf(checkoutStep, topicDeployStep(topicName, PROD)))
      )
   },
   on = PushBuildTrigger(
      push = PushEvent(
         branches = listOf("main")
      )
   ),
   env = mapOf("IMAGE" to "ghcr.io/${dollar}{{ github.repository }}:${dollar}{{ github.sha }}")
)

internal fun kafkaTopicsFrom(req: Request) = req.kafkaTopics.map { topicName ->
   KafkaTopic(
      apiVersion = "kafka.nais.io/v1",
      kind = "Topic",
      metadata = KafkaMetadata(
         name = topicName,
         namespace = req.team,
         labels = mapOf("team" to req.team) + generationLabels
      ),
      spec = KafkaSpec(
         pool = "${dollar}{{kafkaPool}}",
         config = KafkaConfig(
            cleanupPolicy = "delete",
            minimumInSyncReplicas = 1,
            partitions = 1,
            replication = 3,
            retentionBytes = -1,
            retentionHours = 72
         ),
         acl = listOf(
            KafkaAcl(team = req.team, application = req.appName, access = "readwrite"),
            KafkaAcl(team = "anotherteam", application = "anotherapp", access = "read")
         )
      )
   )
}

internal fun alertsFrom(req: Request) = PrometheusRule(
   apiVersion = "monitoring.coreos.com/v1",
   kind = "PrometheusRule",
   metadata = PrometheusRuleMetadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team) + generationLabels
   ),
   spec = PrometheusRuleSpec(
      groups = listOf(
         RuleGroup("${req.appName}-alerts", listOf(
            Rule(
               alert = "${req.appName} er nede",
               description = "App {{ ${dollar}labels.app }} er nede i namespace {{ ${dollar}labels.kubernetes_namespace }}",
               expr = """kube_deployment_status_replicas_unavailable{deployment="${req.appName}"} > 0""",
               forHowLong = "2m",
               action = "kubectl describe pod {{ ${dollar}labels.kubernetes_pod_name }} -n {{ ${dollar}labels.kubernetes_namespace }}` for events, og `kubectl logs {{ ${dollar}labels.kubernetes_pod_name }} -n {{ ${dollar}labels.kubernetes_namespace }}` for logger",
               labels = mapOf("severity" to "warning"),
               annotations = mapOf(
                  "consequence" to "${req.appName} gjør ikke det den skal",
                  "action" to "Fiks den!",
                  "sla" to "Innen 3 timer i kontortid",
                  "documentation" to "https://github.com/navikt/${req.team}/somedoc",
               )
            ),
            Rule(
               alert = "Mye feil i loggene",
               expr = """(100 * sum by (log_app, log_namespace) (rate(logd_messages_total{log_app="${req.appName}",log_level=~"Warning|Error"}[3m])) / sum by (log_app, log_namespace) (rate(logd_messages_total{log_app="${req.appName}"}[3m]))) > 10""",
               forHowLong = "3m",
               action = "Sjekk loggene til app {{ ${dollar}labels.log_app }} i namespace {{ ${dollar}labels.log_namespace }} for å se hvorfor det er så mye feil",
               labels = mapOf("severity" to "critical"),
               annotations = mapOf(
                  "consequence" to "${req.appName} gjør ikke det den skal",
                  "action" to "Fiks den!",
                  "sla" to "Innen 3 timer i kontortid",
                  "documentation" to "https://github.com/navikt/${req.team}/somedoc",
               ),
            )
         ))
      )
   )
)

private fun gcpStuffFrom(req: Request): GCP {
   val sqlInstances = if ((req.extras.contains("postgres")))
      listOf(SQLInstance(type = POSTGRES_13, mapOf("name" to "${req.appName}-db")))
   else emptyList()

   val bigQueryDatasets = if ((req.extras.contains("bigquery")))
      listOf(BigQueryDataset(name = "${req.appName}-dataset", permission = READWRITE, cascadingDelete = false))
   else emptyList()

   return GCP(sqlInstances, bigQueryDatasets)
}

internal fun dockerfileFrom(req: Request) =
   when (req.platform) {
      JVM_GRADLE, JVM_MAVEN -> jvmDockerfileTemplate
      NODEJS -> nodejsDockerfileTemplate
      GO_MAKE -> goDockerfileTemplate
      PYTHON_PIP, PYTHON_POETRY -> pythonDockerfileTemplate
      STATIC_WEB -> staticWebDockerfileTemplate
   }

private fun buildStepsFor(platform: PLATFORM) =
   when (platform) {
      JVM_GRADLE -> gradleJvmBuildSteps
      JVM_MAVEN -> mavenJvmBuildSteps
      NODEJS -> nodejsBuildSteps
      GO_MAKE -> goMakeBuildSteps
      PYTHON_POETRY -> pythonPoetryBuildSteps
      PYTHON_PIP -> pythonPipBuildSteps
      STATIC_WEB -> staticWebBuildSteps
   }

private fun String.base64Encode() = Base64.getEncoder().encodeToString(this.encodeToByteArray())

