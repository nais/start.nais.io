package io.nais

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppModuleTest {
   val firefoxMac = "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:42.0) Gecko/20100101 Firefox/42.0"
   val googleBot = "Googlebot"

   @Test
   fun `User-Agents are only interesting after the last space`() {
      val parsed = parse(firefoxMac)
      assertEquals("Firefox/42.0", parsed)
   }

   @Test
   fun `the whole name is used if it doesn't contain spaces `() {
      val parsed = parse(googleBot)
      assertEquals(googleBot, parsed)
   }
}
