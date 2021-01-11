package io.nais

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException

@ExperimentalSerializationApi
@Suppress("unused") // referenced in application.conf
fun Application.main() {

   install(ContentNegotiation) {
      json(contentType = ContentType.Application.Json)
   }

   install(StatusPages) {
      exception<SerializationException> { cause ->
         call.respond(BadRequest, "Unable to parse JSON: ${cause.message}")
      }
   }

   routing {
      get("/") {
         call.respondRedirect("/index.html")
      }

      static("/") {
         resources("web")
      }

      app()
      observability()
   }
}
