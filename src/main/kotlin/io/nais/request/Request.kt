package io.nais.request

import kotlinx.serialization.Serializable

@Serializable
class Request (
   val team: String,
   val appName: String,
   val platform: PLATFORM
)

enum class PLATFORM {
   JVM_GRADLE, JVM_MAVEN, NODEJS
}

