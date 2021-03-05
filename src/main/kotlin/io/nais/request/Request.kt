package io.nais.request

import kotlinx.serialization.Serializable

@Serializable
data class Request (
   val team: String,
   val appName: String,
   val platform: PLATFORM,
   val extras: List<String>
)

enum class PLATFORM {
   JVM_GRADLE, JVM_MAVEN, NODEJS, GO_MAKE
}

