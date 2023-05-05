package io.nais

import io.nais.Environment.DEV
import io.nais.Environment.PROD
import io.nais.PLATFORM.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URL

@ExperimentalSerializationApi
class RequestResponseMappingTest {

   @Test
   fun `basic nais app is generated from request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN)
      val naisApp = naisApplicationFrom(request)
      assertEquals(request.appName, naisApp.metadata.name)
      assertEquals(request.team, naisApp.metadata.namespace)
   }

   @Test
   fun `alerts are generated from request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN)
      val alerts = alertsFrom(request)
      assertEquals(request.appName, alerts.metadata.name)
      assertEquals(request.team, alerts.metadata.labels["team"])
      assertEquals("Fiks den!", alerts.spec.groups[0].rules[0].annotations["action"])
   }

   @Test
   fun `deploy to dev job depends on build`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE)
      val workflow = gitHubWorkflowFrom(request)
      assertTrue(workflow.jobs["deployAppToDev"]?.needs?.contains("build") ?: false)
   }

   @Test
   fun `deploy to prod job depends on deploy to dev`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE)
      val workflow = gitHubWorkflowFrom(request)
      assertTrue(workflow.jobs["deployAppToProd"]?.needs?.contains("deployAppToDev") ?: false)
   }

   @Test
   fun `workflow for gradle jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_GRADLE)
      val workflow = gitHubWorkflowFrom(request)
      val gradleBuildStep = workflow.jobs["build"]?.steps?.firstOrNull { it.run?.contains("gradle") ?: false }
      assertNotNull(gradleBuildStep)
   }

   @Test
   fun `workflow for maven jvm app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = JVM_MAVEN)
      val workflow = gitHubWorkflowFrom(request)
      val mavenBuildStep = workflow.jobs["build"]?.steps?.firstOrNull { it.run?.contains("mvn") ?: false }
      assertNotNull(mavenBuildStep)
   }

   @Test
   fun `workflow for python with poetry app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = PYTHON_POETRY)
      val workflow = gitHubWorkflowFrom(request)
      val poetryBuildStep = workflow.jobs["build"]?.steps?.firstOrNull { it.run?.contains("poetry") ?: false }
      assertNotNull(poetryBuildStep)
   }

   @Test
   fun `workflow for python with pip app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = PYTHON_PIP)
      val workflow = gitHubWorkflowFrom(request)
      val pipBuildStep = workflow.jobs["build"]?.steps?.firstOrNull { it.run?.contains("pip") ?: false }
      assertNotNull(pipBuildStep)
   }

   @Test
   fun `workflow for nodejs app`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS)
      val workflow = gitHubWorkflowFrom(request)
      val nodeBuildStep = workflow.jobs["build"]?.steps?.firstOrNull { it.run?.contains("npm") ?: false }
      assertNotNull(nodeBuildStep)
   }

   @Test
   fun `idporten is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("idporten"))
      val naisApp = naisApplicationFrom(request)
      assertNotNull(naisApp.spec.idPorten)
   }

   @Test
   fun `openSearch is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("openSearch"))
      val naisApp = naisApplicationFrom(request)
      assertNotNull(naisApp.spec.openSearch)
   }

   @Test
   fun `azure ad is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("aad"))
      val naisApp = naisApplicationFrom(request)
      assertNotNull(naisApp.spec.azure)
   }

   @Test
   fun `postgres db is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("postgres"))
      val naisApp = naisApplicationFrom(request)
      assertEquals(1, naisApp.spec.gcp?.sqlInstances?.size)
   }

   @Test
   fun `bigquery dataset is added upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("bigquery"))
      val naisApp = naisApplicationFrom(request)
      assertEquals(1, naisApp.spec.gcp?.bigQueryDatasets?.size)
   }

   @Test
   fun `postgres and bigquery can be added simultaneously`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, extras = listOf("bigquery", "postgres"))
      val naisApp = naisApplicationFrom(request)
      assertEquals(1, naisApp.spec.gcp?.bigQueryDatasets?.size)
      assertEquals(1, naisApp.spec.gcp?.sqlInstances?.size)
   }

   @Test
   fun `kafka topics are created upon request`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val topics = kafkaTopicsFrom(request)
      assertEquals(1, topics.size)
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
   fun `ingresses are built distinctly based on environment`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val devVars = appVarsFrom(request, DEV)
      val prodVars = appVarsFrom(request, PROD)
      assertEquals(URL("https://mycoolapp.intern.dev.nav.no"), devVars.ingresses.first())
      assertEquals(URL("https://mycoolapp.intern.nav.no"), prodVars.ingresses.first())
   }

   @Test
   fun `deployment of kafka topics is added to workflow`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val workflow = gitHubWorkflowFrom(request)
      assertTrue(workflow.jobs.containsKey("deployTopicMytopicDev"))
      assertTrue(workflow.jobs.containsKey("deployTopicMytopicProd"))
   }

   @Test
   fun `deployment of alerts is added to workflow`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS, kafkaTopics = listOf("mytopic"))
      val workflow = gitHubWorkflowFrom(request)
      assertTrue(workflow.jobs.containsKey("deployAlertsToDev"))
      assertTrue(workflow.jobs.containsKey("deployAlertsToProd"))
   }

   @Test
   fun `dockerfile for the requested platform is added`() {
      val request = Request(team = "myteam", appName = "mycoolapp", platform = NODEJS)
      val workflow = gitHubWorkflowFrom(request)
      assertTrue(workflow.jobs.containsKey("deployAlertsToDev"))
      assertTrue(workflow.jobs.containsKey("deployAlertsToProd"))
   }

}
