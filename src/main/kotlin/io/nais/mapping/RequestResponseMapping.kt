package io.nais.mapping

import io.nais.deploy.*
import io.nais.naisapp.*
import io.nais.naisapp.DatabaseType.POSTGRES_13
import io.nais.naisapp.Environment.DEV
import io.nais.request.PLATFORM
import io.nais.request.PLATFORM.*
import io.nais.request.Request
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import java.net.URL

private const val dollar = '$' // workaround, escaping doesn't work in multiline strings (https://youtrack.jetbrains.com/issue/KT-2425)

@ExperimentalSerializationApi
fun naisApplicationFrom(req: Request) = NaisApplication(
   apiVersion = "nais.io/v1alpha1",
   kind = "Application",
   metadata = Metadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team)
   ),
   spec = Spec(
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
   ingresses = listOf(URL("https://${req.appName}${if (env == DEV) ".dev" else ""}.intern.nav.no"))
)

fun gitHubWorkflowFrom(req: Request) = GitHubWorkflow(
   name = "Build and deploy ${req.appName}",
   jobs = Jobs(
      build = Job(name = "build", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + buildStepsFor(req.platform)),
      deployToDev = Job(name = "Deploy to dev", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("dev-gcp")),
      deployToProd = Job(name = "Deploy to prod", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("prod-gcp"))
   ),
   on = PushBuildTrigger(
      push = PushEvent(
         branches = listOf("main")
      )
   ),
   env = mapOf("IMAGE" to "docker.pkg.github.com/${dollar}{{ github.repository }}/${req.appName}:${dollar}{{ github.sha }}")
)

private fun buildStepsFor(platform: PLATFORM) =
   when (platform) {
      JVM_GRADLE -> gradleJvmBuildSteps
      JVM_MAVEN -> mavenJvmBuildSteps
      NODEJS -> nodejsBuildSteps
   }

