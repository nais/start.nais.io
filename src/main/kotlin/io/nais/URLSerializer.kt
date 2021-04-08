package io.nais

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.URL

@Serializer(forClass = URL::class)
@ExperimentalSerializationApi
object URLSerializer : KSerializer<URL> {
   override val descriptor = PrimitiveSerialDescriptor("URL", PrimitiveKind.STRING)

   override fun deserialize(decoder: Decoder): URL {
      return URL(decoder.decodeString())
   }

   override fun serialize(encoder: Encoder, value: URL) {
      encoder.encodeString(value.toString())
   }
}
