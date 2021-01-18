package io.nais.naisapp

import io.nais.testdata.basicNaisYaml
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class NaisApplicationTest {

   @Test
   fun `minimal app with supplied image`() {
      val app = NaisApplication(
         metadata = Metadata(
            name = "mycoolapp",
            namespace = "myteam",
            labels = mapOf("team" to "myteam")
         ),
         spec = Spec()
      )
      assertEquals(basicNaisYaml, app.serialize())
   }

   @Test
   fun `minimal app without supplied image`() {
      val app = NaisApplication(
         metadata = Metadata(
            name = "mycoolapp",
            namespace = "myteam",
            labels = mapOf("team" to "myteam")
         ),
         spec = Spec()
      )
      assertTrue(app.serialize().contains("image: {{image}}"))
   }

}
