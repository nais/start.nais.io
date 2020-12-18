package io.nais

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

@Suppress("unused") // referenced in application.conf
fun Application.main() {
   routing {
      get("/") {
         call.respond("Hello")
      }
   }
}
