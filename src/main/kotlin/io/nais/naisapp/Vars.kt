package io.nais.naisapp

import com.charleskorn.kaml.Yaml
import io.nais.serialize.URLSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
@ExperimentalSerializationApi
data class Vars(
   val ingresses: List<@Serializable(with = URLSerializer::class) URL>
)

@ExperimentalSerializationApi
fun Vars.asYaml() = Yaml.default.encodeToString(Vars.serializer(), this)

enum class Environment {
   DEV, PROD
}
