package io.nais.deploy

import io.nais.testdata.gradleJvmWorkflowYaml
import io.nais.testdata.nodejsWorkflowYaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GitHubWorkflowTest {

   @Test
   fun `build and deploy gradle jvm app`() {
      val actual = GitHubWorkflow(
         name = "tulleflow",
         env = mapOf("IMAGE" to "docker.pkg.github.com/org/app:version"),
         jobs = Jobs(
            build = Job(name = "build", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + gradleKotlinBuildSteps),
            deployToDev = Job(name = "Deploy to dev", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("dev-gcp")),
            deployToProd = Job(name = "Deploy to prod", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("prod-gcp"))
         ),
         on = PushBuildTrigger(
            push = PushEvent(
               branches = listOf("main")
            )
         )
      )
      assertEquals(gradleJvmWorkflowYaml, actual.asYaml())
   }

   @Test
   fun `build and deploy nodejs app`() {
      val actual = GitHubWorkflow(
         name = "tulleflow",
         env = mapOf("IMAGE" to "docker.pkg.github.com/org/app:version"),
         jobs = Jobs(
            build = Job(name = "build", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + nodejsBuildSteps),
            deployToDev = Job(name = "Deploy to dev", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("dev-gcp")),
            deployToProd = Job(name = "Deploy to prod", runsOn = "ubuntu-latest", steps = listOf(checkoutStep) + deploySteps("prod-gcp"))
         ),
         on = PushBuildTrigger(
            push = PushEvent(
               branches = listOf("main")
            )
         )
      )
      assertEquals(nodejsWorkflowYaml, actual.asYaml())
   }

}

