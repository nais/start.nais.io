package io.nais

import io.nais.DatabaseType.POSTGRES_13
import io.nais.Environment.DEV
import io.nais.Environment.PROD
import io.nais.PLATFORM.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import java.io.OutputStream
import java.net.URL
import java.nio.file.Paths
import java.util.*

private const val dollar = '$' // workaround, escaping doesn't work in multiline strings (https://youtrack.jetbrains.com/issue/KT-2425)

@ExperimentalSerializationApi
fun naisApplicationFrom(req: Request) = NaisApplication(
   apiVersion = "nais.io/v1alpha1",
   kind = "Application",
   metadata = AppMetadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team)
   ),
   spec = AppSpec(
      image = "##REPLACE_IMAGE##",
      liveness = StatusEndpoint(path = "/isalive", port = 8080, initialDelay = 20, timeout = 60),
      readiness = StatusEndpoint(path = "/isready", port = 8080, initialDelay = 20, timeout = 60),
      replicas = Replicas(min = 2, max = 2, cpuThresholdPercentage = 50),
      prometheus = Prometheus(enabled = true, path = "/metrics"),
      limits = Resources(cpu = "200m", memory = "256Mi"),
      requests = Resources(cpu = "200m", memory = "256Mi"),
      ingresses = "##REPLACE_INGRESS##"
   ).apply {
      req.extras.forEach { feature ->
         when (feature) {
            "idporten" -> idPorten = IdPorten(true)
            "aad" -> azure = Azure(application = AzureApplication(enabled = true))
            "postgres" -> gcp = GCP(
               sqlInstances = listOf(
                  SQLInstance(type = POSTGRES_13,
                     mapOf("name" to "${req.appName}-db"))
               )
            )
            else -> throw SerializationException("dont't know anything about '$feature'")
         }
      }
   }
)

@ExperimentalSerializationApi
fun appVarsFrom(req: Request, env: Environment) = Vars(
   ingresses = listOf(URL("https://${req.appName}${if (env == DEV) ".dev" else ""}.intern.nav.no")),
   kafkaPool = if (req.kafkaTopics.isNotEmpty()) "nais-${env.name.lowercase()}" else null
)

fun gitHubWorkflowFrom(req: Request) = GitHubWorkflow(
   name = "Build and deploy ${req.appName}",
   jobs = mapOf(
      "build" to Job(name = "build", runsOn = "ubuntu-18.04", steps = listOf(checkoutStep) + buildStepsFor(req.platform)),
      "deployAppToDev" to Job(name = "Deploy to dev", needs = "build", runsOn = "ubuntu-18.04", steps = listOf(checkoutStep, appDeployStep(DEV))),
      "deployAppToProd" to Job(name = "Deploy to prod", needs = "deployAppToDev", runsOn = "ubuntu-18.04", steps = listOf(checkoutStep, appDeployStep(PROD)))
   ) + req.kafkaTopics.flatMap { topicName ->
      listOf(
         "deployTopic${topicName.replaceFirstChar { it.titlecase() }}Dev" to Job(name = "Deploy Kafka topic $topicName to dev", runsOn = "ubuntu-18.04", steps = listOf(checkoutStep, topicDeployStep(topicName, DEV))),
         "deployTopic${topicName.replaceFirstChar { it.titlecase() }}Prod" to Job(name = "Deploy Kafka topic $topicName to prod", runsOn = "ubuntu-18.04", steps = listOf(checkoutStep, topicDeployStep(topicName, PROD)))
      )
   },
   on = PushBuildTrigger(
      push = PushEvent(
         branches = listOf("main")
      )
   ),
   env = mapOf("IMAGE" to "docker.pkg.github.com/${dollar}{{ github.repository }}/${req.appName}:${dollar}{{ github.sha }}")
)

fun kafkaTopicsFrom(req: Request) = req.kafkaTopics.map { topicName ->
   KafkaTopic(
      apiVersion = "kafka.nais.io/v1",
      kind = "Topic",
      metadata = KafkaMetadata(
         name = topicName,
         namespace = req.team,
         labels = mapOf("team" to req.team)
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

@ExperimentalSerializationApi
fun yamlFilesFrom(request: Request) = mapOf(
   ".nais/nais.yaml" to naisApplicationFrom(request).serialize(),
   ".nais/dev.yaml" to appVarsFrom(request, DEV).serialize(),
   ".nais/prod.yaml" to appVarsFrom(request, PROD).serialize(),
   ".github/workflows/main.yaml" to gitHubWorkflowFrom(request).serialize()
) + kafkaTopicsFrom(request).map { topic ->
   ".nais/topic-${topic.metadata.name}.yaml" to topic.serialize()
}

@ExperimentalSerializationApi
fun Map<String, String>.asJson() = mapValues { it.value.toBase64() }

@ExperimentalSerializationApi
fun Map<String, String>.asZipStream(outputStream: OutputStream) =
   zipTo(outputStream, mapKeys { Paths.get(it.key) })

private fun String.toBase64() = Base64.getEncoder().encodeToString(this.encodeToByteArray())

private fun buildStepsFor(platform: PLATFORM) =
   when (platform) {
      JVM_GRADLE -> gradleJvmBuildSteps
      JVM_MAVEN -> mavenJvmBuildSteps
      NODEJS -> nodejsBuildSteps
      GO_MAKE -> goMakeBuildSteps
   }

