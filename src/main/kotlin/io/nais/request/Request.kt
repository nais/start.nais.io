package io.nais.request

import kotlinx.serialization.Serializable

@Serializable
class Request (
   val team: String,
   val appName: String,
   val image: String
)
