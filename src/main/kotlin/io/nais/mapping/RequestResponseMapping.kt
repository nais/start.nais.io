package io.nais.mapping

import io.nais.deploy.*
import io.nais.naisapp.*
import io.nais.naisapp.Environment.DEV
import io.nais.request.PLATFORM
import io.nais.request.PLATFORM.*
import io.nais.request.Request
import kotlinx.serialization.ExperimentalSerializationApi
import java.net.URL

private val dollar = '$' // workaround, escaping doesn't work in multiline strings (https://youtrack.jetbrains.com/issue/KT-2425)

@ExperimentalSerializationApi
fun naisApplicationFrom(req: Request) = NaisApplication(
   metadata = Metadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team)
   ),
   spec = Spec()
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
