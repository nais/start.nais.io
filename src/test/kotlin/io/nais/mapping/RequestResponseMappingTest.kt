package io.nais.mapping

import io.nais.deploy.serialize
import io.nais.naisapp.Environment.DEV
import io.nais.naisapp.Environment.PROD
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
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN)
      val app = naisApplicationFrom(request)
      assertEquals(basicNaisYaml, app.serialize())
   }

   @Test
   fun `deploy to dev job depends on build`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE)
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("""needs: "build"""))
   }

   @Test
   fun `deploy to prod job depends on deploy to dev`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE)
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("""needs: "deployToDev"""))
   }

   @Test
   fun `workflow for gradle jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE)
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("gradle"))
      assertFalse(yaml.contains("mvn"))
      assertFalse(yaml.contains("npm"))
   }

   @Test
   fun `workflow for maven jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN)
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("mvn"))
      assertFalse(yaml.contains("gradle"))
      assertFalse(yaml.contains("npm"))
   }

   @Test
   fun `workflow for nodejs app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS)
      val yaml = gitHubWorkflowFrom(request).serialize()
      assertTrue(yaml.contains("npm"))
      assertFalse(yaml.contains("mvn"))
      assertFalse(yaml.contains("gradle"))
   }

   @Test
   fun `idporten is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("idporten"))
      val yaml = naisApplicationFrom(request).serialize()
      assertTrue(yaml.contains("idPorten"))
   }

   @Test
   fun `azure ad is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("aad"))
      val yaml = naisApplicationFrom(request).serialize()
      assertTrue(yaml.contains("azure"))
   }

   @Test
   fun `postgres db is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("postgres"))
      val yaml = naisApplicationFrom(request).serialize()
      assertTrue(yaml.contains("sqlInstances"))
   }

   @Test
   fun `kafka topics are created upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val yaml = kafkaTopicsFrom(request).map { it.serialize() }
      assertTrue(yaml[0].contains("kafka.nais.io"))
   }

   @Test
   fun `kafka pool names are added to vars based on environment`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val devVars = appVarsFrom(request, DEV)
      val prodVars = appVarsFrom(request, PROD)
      assertEquals("nais-dev", devVars.kafkaPool)
      assertEquals("nais-prod", prodVars.kafkaPool)
   }

   @Test
   fun `deployment of kafka topics is added to workflow`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val workflow = gitHubWorkflowFrom(request)
      assertTrue(workflow.jobs.containsKey("deployTopicMytopicDev"))
      assertTrue(workflow.jobs.containsKey("deployTopicMytopicProd"))
   }

}
