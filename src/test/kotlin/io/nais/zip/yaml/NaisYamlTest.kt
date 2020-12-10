package io.nais.zip.yaml

import io.nais.yaml.NaisApp
import io.nais.yaml.createNaisYaml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class NaisYamlTest {

   @Test
   fun `create a minimal NAIS Application yaml`() {
      val app = NaisApp("tulleapp", "tulleteam", "registry/image:version")
      val expected = File({}.javaClass.getResource("/naisyaml/nais.yaml").toURI()).readText()
      val actual = createNaisYaml(app)
      assertEquals(expected, actual)
   }

}
