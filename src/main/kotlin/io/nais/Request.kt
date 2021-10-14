package io.nais

import kotlinx.serialization.Serializable

private val gcpOnlyFeatures = listOf("postgres", "bigquery")

@Serializable
data class Request (
   val team: String,
   val appName: String,
   val platform: PLATFORM,
   val extras: List<String> = emptyList(),
   val kafkaTopics: List<String> = emptyList()
) {
   fun containsGcpSpecificThings(): Boolean =
      extras.any { gcpOnlyFeatures.contains(it) }
}

enum class PLATFORM {
   JVM_GRADLE, JVM_MAVEN, NODEJS, GO_MAKE
}

