package io.nais.mapping

import io.nais.deploy.serialize
import io.nais.naisapp.serialize
import io.nais.request.PLATFORM.*
import io.nais.request.Request
import io.nais.testdata.basicNaisYaml
import io.nais.testdata.gradleJvmWorkflowYaml
import io.nais.testdata.mavenJvmWorkflowYaml
import io.nais.testdata.nodejsWorkflowYaml
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class RequestResponseMappingTest {

   @Test
   fun `nais yaml for app with no extras`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN, emptyList())
      val app = naisApplicationFrom(request)
      assertEquals(basicNaisYaml, app.serialize())
   }

   @Test
   fun `workflow for gradle jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE, emptyList())
      val workflow = gitHubWorkflowFrom(request)
      assertEquals(gradleJvmWorkflowYaml, workflow.serialize())
   }

   @Test
   fun `workflow for maven jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN, emptyList())
      val workflow = gitHubWorkflowFrom(request)
      assertEquals(mavenJvmWorkflowYaml, workflow.serialize())
   }

   @Test
   fun `workflow for nodejs app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, emptyList())
      val workflow = gitHubWorkflowFrom(request)
      assertEquals(nodejsWorkflowYaml, workflow.serialize())
   }

}
