package io.nais.mapping

import io.nais.deploy.*
import io.nais.naisapp.*
import io.nais.naisapp.Environment.DEV
import io.nais.request.PLATFORM
import io.nais.request.PLATFORM.JVM_GRADLE
import io.nais.request.PLATFORM.NODEJS
import io.nais.request.Request
import kotlinx.serialization.ExperimentalSerializationApi
import java.net.URL

@ExperimentalSerializationApi
fun naisApplicationFrom(req: Request) = NaisApplication(
   metadata = Metadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team)
   ),
   spec = Spec(
      image = req.image,
   )
)

@ExperimentalSerializationApi
fun appVarsFrom(req: Request, env: Environment) = Vars(
   ingresses = listOf(URL("https://${req.appName}${if (env == DEV) ".dev" else ""}.intern.nav.no"))
)

fun gitHubWorkflowFrom(req: Request) = GitHubWorkflow(
   name = "Build and deploy ${req.appName}",
   env = mapOf("IMAGE" to req.image),
   jobs = Jobs(
      build = Job(name = "build", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + buildStepsFor(req.platform)),
      deployToDev = Job(name = "Deploy to dev", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("dev-gcp")),
      deployToProd = Job(name = "Deploy to prod", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("prod-gcp"))
   ),
   on = PushBuildTrigger(
      push = PushEvent(
         branches = listOf("main")
      )
   )
)

private fun buildStepsFor(platform: PLATFORM) =
   when (platform) {
      JVM_GRADLE -> gradleJvmBuildSteps
      NODEJS -> nodejsBuildSteps
   }
