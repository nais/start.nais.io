package io.nais

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@ExperimentalSerializationApi
data class NaisApplication(
   val apiVersion: String,
   val kind: String,
   val metadata: AppMetadata,
   val spec: AppSpec
)

@ExperimentalSerializationApi
fun NaisApplication.serialize() =
   Yaml(configuration = YamlConfiguration(encodeDefaults = false))
.encodeToString(NaisApplication.serializer(), this)
      .replace(""""##REPLACE_INGRESS##"""", """
{{#each ingresses as |url|}}
- {{url}}
{{/each}}""")
      .replace(""""##REPLACE_IMAGE##"""", "{{image}}")

@Serializable
data class AppMetadata(
   val name: String,
   val namespace: String,
   val labels: Map<String, String>
)

@Serializable
@ExperimentalSerializationApi
data class AppSpec(
   val image: String,
   val liveness: StatusEndpoint,
   val readiness: StatusEndpoint,
   val replicas: Replicas,
   val prometheus: Prometheus,
   val limits: Resources,
   val requests: Resources,
   val ingresses: String,
   var azure: Azure? = null,
   var idPorten: IdPorten? = null,
   var elastic: Elastic? = null,
   var gcp: GCP? = null
)

@Serializable
data class StatusEndpoint(
   val path: String,
   val port: Int,
   val initialDelay: Int,
   val timeout: Int
)

@Serializable
data class Replicas(
   val min: Int,
   val max : Int,
   val cpuThresholdPercentage: Int
)

@Serializable
data class Prometheus(
   val enabled: Boolean,
   val path: String
)

@Serializable
data class Resources(
   val cpu: String,
   val memory: String
)

@Serializable
data class IdPorten(
   val enabled: Boolean
)

@Serializable
data class Elastic(
   val instance: String
)

@Serializable
data class Azure(
   val application: AzureApplication
)

@Serializable
data class AzureApplication(
   val enabled: Boolean
)

@Serializable
data class GCP(
   val sqlInstances: List<SQLInstance> = emptyList(),
   val bigQueryDatasets: List<BigQueryDataset> = emptyList()
)

@Serializable
data class SQLInstance(
   val type: DatabaseType,
   val databases: Map<String, String>
)

@Serializable
data class BigQueryDataset(
   val permission: BigQueryDatasetPermission,
   val name: String,
   val cascadingDelete: Boolean
)

enum class DatabaseType {
   POSTGRES_13
}

enum class BigQueryDatasetPermission {
   READWRITE
}


