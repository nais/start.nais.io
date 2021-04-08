package io.nais

import kotlinx.serialization.Serializable

@Serializable
data class Request (
   val team: String,
   val appName: String,
   val platform: PLATFORM,
   val extras: List<String> = emptyList(),
   val kafkaTopics: List<String> = emptyList()
)

enum class PLATFORM {
   JVM_GRADLE, JVM_MAVEN, NODEJS, GO_MAKE
}

