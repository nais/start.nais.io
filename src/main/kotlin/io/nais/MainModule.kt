package io.nais

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.main() {
   routing {
      get("/") {
         call.respond("Hello")
      }
   }
}
