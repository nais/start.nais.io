package io.nais

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.Serializable

@Serializable
data class KafkaTopic(
   val apiVersion: String,
   val kind: String,
   val metadata: KafkaMetadata,
   val spec: KafkaSpec
)

fun KafkaTopic.serialize() = Yaml(configuration = YamlConfiguration(encodeDefaults = false))
   .encodeToString(KafkaTopic.serializer(), this)

@Serializable
data class KafkaMetadata(
   val name: String,
   val namespace: String,
   val labels: Map<String, String>,
   val annotations: Map<String, String>
)

@Serializable
data class KafkaSpec(
   val pool: String,
   val config: KafkaConfig,
   val acl: List<KafkaAcl>
)

@Serializable
data class KafkaConfig(
   val cleanupPolicy: String,
   val minimumInSyncReplicas: Int,
   val partitions: Int,
   val replication: Int,
   val retentionBytes: Int,
   val retentionHours: Int
)

@Serializable
data class KafkaAcl(
   val team: String,
   val application: String,
   val access: String
)
