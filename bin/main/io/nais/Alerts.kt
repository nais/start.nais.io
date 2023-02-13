package io.nais

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alerts(
   val apiVersion: String,
   val kind: String,
   val metadata: AlertMetadata,
   val spec: AlertSpec
)

@ExperimentalSerializationApi
fun Alerts.serialize() =
   Yaml(configuration = YamlConfiguration(encodeDefaults = false))
      .encodeToString(Alerts.serializer(), this)

@Serializable
data class AlertMetadata(
   val name: String,
   val namespace: String,
   val labels: Map<String, String>
)

@Serializable
data class AlertSpec(
   val receivers: AlertReceivers,
   val alerts: List<Alert>
)

@Serializable
data class AlertReceivers(
   val slack: Slack
)

@Serializable
data class Slack(
   val channel: String,
   val prependText: String
)

@Serializable
data class Alert(
   val alert: String,
   val description: String? = null,
   val expr: String,
   @SerialName("for")
   val forHowLong: String,
   val action: String,
   val documentation: String? = null,
   val sla: String,
   val severity: String
)
