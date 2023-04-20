package io.nais

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrometheusRule(
   val apiVersion: String,
   val kind: String,
   val metadata: PrometheusRuleMetadata,
   val spec: PrometheusRuleSpec
)

@ExperimentalSerializationApi
fun PrometheusRule.serialize() =
   Yaml(configuration = YamlConfiguration(encodeDefaults = false))
      .encodeToString(PrometheusRule.serializer(), this)

@Serializable
data class PrometheusRuleMetadata(
   val name: String,
   val namespace: String,
   val labels: Map<String, String>
)

@Serializable
data class PrometheusRuleSpec(
   val groups: List<RuleGroup>
)

@Serializable
data class RuleGroup(
   val name: String,
   val rules: List<Rule>
)

@Serializable
data class Rule(
   val alert: String,
   val description: String? = null,
   val expr: String,
   @SerialName("for")
   val forHowLong: String,
   val action: String,
   val labels: Map<String, String> = emptyMap(),
   val annotations: Map<String, String> = emptyMap()
)
