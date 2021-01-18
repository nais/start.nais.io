package io.nais.naisapp

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@ExperimentalSerializationApi
data class NaisApplication(
   val apiVersion: String = "nais.io/v1alpha1",
   val kind: String = "Application",
   val metadata: Metadata,
   val spec: Spec
)

@ExperimentalSerializationApi
fun NaisApplication.serialize() =
   Yaml.default.encodeToString(NaisApplication.serializer(), this)
      .let {
         it.replace(""""##REPLACE_INGRESS##"""", """
    {{#each ingresses as |url|}}
      - {{url}}
    {{/each}}""")
      }

@Serializable
data class Metadata(
   val name: String,
   val namespace: String,
   val labels: Map<String, String> = emptyMap()
)

@Serializable
@ExperimentalSerializationApi
data class Spec(
   val image: String,
   val liveness: StatusEndpoint = StatusEndpoint(path = "/isalive"),
   val readiness: StatusEndpoint = StatusEndpoint(path = "/isready"),
   val replicas: Replicas = Replicas(),
   val prometheus: Prometheus = Prometheus(path = "/metrics"),
   val limits: Resources = Resources(),
   val requests: Resources = Resources(),
   val ingresses: String = "##REPLACE_INGRESS##"
)

@Serializable
data class StatusEndpoint(
   val path: String,
   val port: Int = 80,
   val initialDelay: Int = 20,
   val timeout: Int = 1
)

@Serializable
data class Replicas(
   val min: Int = 2,
   val max : Int = 2,
   val cpuThresholdPercentage: Int = 50
)

@Serializable
data class Prometheus(
   val enabled: Boolean = true,
   val path: String
)

@Serializable
data class Resources(
   val cpu: String = "200m",
   val memory: String = "256Mi"
)
