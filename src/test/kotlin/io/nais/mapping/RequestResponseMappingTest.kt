package io.nais.mapping

import io.nais.deploy.serialize
import io.nais.naisapp.serialize
import io.nais.request.PLATFORM.*
import io.nais.request.Request
import io.nais.testdata.basicNaisYaml
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.*
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
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("gradle"))
      assertFalse(yaml.contains("mvn"))
      assertFalse(yaml.contains("npm"))
   }

   @Test
   fun `workflow for maven jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN, emptyList())
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("mvn"))
      assertFalse(yaml.contains("gradle"))
      assertFalse(yaml.contains("npm"))
   }

   @Test
   fun `workflow for nodejs app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, emptyList())
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("npm"))
      assertFalse(yaml.contains("mvn"))
      assertFalse(yaml.contains("gradle"))
   }

   @Test
   fun `idporten is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, listOf("idporten"))
      val yaml = naisApplicationFrom(request).serialize()
      assertTrue(yaml.contains("idPorten"))
   }

   @Test
   fun `azure ad is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, listOf("aad"))
      val yaml = naisApplicationFrom(request).serialize()
      assertTrue(yaml.contains("azure"))
   }

   @Test
   fun `postgres db is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, listOf("postgres"))
      val yaml = naisApplicationFrom(request).serialize()
      assertTrue(yaml.contains("sqlInstances"))
   }

}
