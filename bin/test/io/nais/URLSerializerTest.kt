package io.nais

import com.charleskorn.kaml.Yaml
import io.nais.URLSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URL

@ExperimentalSerializationApi
class URLSerializerTest {

   @Test
   fun serialize() {
      val url = URL("http://example.com")
      val expected = """"$url""""
      val actual = Yaml.default.encodeToString(URLSerializer, url)
      assertEquals(expected, actual)
   }

   @Test
   fun deserialize() {
      val expected = URL("http://example.com")
      val actual = Yaml.default.decodeFromString(URLSerializer, """"http://example.com"""")
      assertEquals(expected, actual)
   }

}

