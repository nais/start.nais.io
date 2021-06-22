package io.nais

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Application.Zip
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnsupportedMediaType
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException

@ExperimentalSerializationApi
@Suppress("unused") // referenced in application.conf
fun Application.mainModule() {

   install(ContentNegotiation) {
      json()
   }

   install(StatusPages) {
      exception<SerializationException> { cause ->
         call.respond(BadRequest, "Unable to parse JSON: ${cause.message}")
      }

      exception<BadContentTypeFormatException> { cause ->
         call.respond(UnsupportedMediaType, cause.message ?: "Don't know how to serve this weird Content-Type")
      }
   }

   routing {
      static("/") {
         resources("web")
         defaultResource("index.html", "web")
      }

      app()
   }
}

@ExperimentalSerializationApi
fun Route.app() {
   post("/app") {
      val request = call.receive<Request>()
      val response = serve(request)
      val requestedFormat = ContentType.parse(call.request.accept() ?: "application/json")
      when (requestedFormat) {
         Zip -> call.respondOutputStream(Zip, OK) { response.zipToStream(this) }
         Json -> call.respond(response.asJson())
         else -> call.respond(UnsupportedMediaType)
      }
      Metrics.countNewDownload(request.team, request.platform, requestedFormat.toString())
   }
}


