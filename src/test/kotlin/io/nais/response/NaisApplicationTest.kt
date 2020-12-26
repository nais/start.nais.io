package io.nais.response

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class NaisApplicationTest {

   @Test
   fun `minimal app`() {
      val app = NaisApplication(
         metadata = Metadata(
            name = "mycoolapp",
            namespace = "myteam",
            labels = mapOf("team" to "myteam")
         ),
         spec = Spec(
            image = "something/whatever:1"
         )
      )
      assertEquals(minimalYaml, app.asYaml())
   }

   private val minimalYaml = """
      apiVersion: "nais.io/v1alpha1"
      kind: "Application"
      metadata:
        name: "mycoolapp"
        namespace: "myteam"
        labels:
          "team": "myteam"
      spec:
        image: "something/whatever:1"
        liveness:
          path: "/isalive"
          port: 80
          initialDelay: 20
          timeout: 1
        readiness:
          path: "/isready"
          port: 80
          initialDelay: 20
          timeout: 1
        replicas:
          min: 2
          max: 2
          cpuThresholdPercentage: 50
        prometheus:
          enabled: true
          path: "/metrics"
        limits:
          cpu: "200m"
          memory: "256Mi"
        requests:
          cpu: "200m"
          memory: "256Mi"
        ingresses: []
         """.trimIndent()

}
