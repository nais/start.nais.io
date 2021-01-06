package io.nais.naisapp

import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URL

@ExperimentalSerializationApi
class VarsTest {

   @Test
   fun `list of ingresses`() {
      val vars = Vars(listOf(URL("http://localhost")))
      val expected = """
         ingresses:
         - "http://localhost"
      """.trimIndent()
      assertEquals(expected, vars.asYaml())
   }

}
