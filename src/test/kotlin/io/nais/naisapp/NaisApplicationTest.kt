package io.nais.naisapp

import io.nais.testdata.basicNaisYaml
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class NaisApplicationTest {

   @Test
   fun `minimal app`() {
      val app = NaisApplication(
         metadata = Metadata(
            name = "mycoolapp",
            namespace = "myteam",
            labels = mapOf("team" to "myteam")
         ),
         spec = Spec(
            image = "something/whatever:1"
         )
      )
      assertEquals(basicNaisYaml, app.serialize())
   }

}
