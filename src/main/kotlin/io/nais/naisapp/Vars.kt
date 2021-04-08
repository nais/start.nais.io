package io.nais.naisapp

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import io.nais.serialize.URLSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
@ExperimentalSerializationApi
data class Vars(
   val ingresses: List<@Serializable(with = URLSerializer::class) URL>,
   val kafkaPool: String? = null
)

@ExperimentalSerializationApi
fun Vars.serialize() = Yaml(configuration = YamlConfiguration(encodeDefaults = false))
   .encodeToString(Vars.serializer(), this)

enum class Environment {
   DEV, PROD
}
