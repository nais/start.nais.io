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
import io.ktor.util.pipeline.*
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
      intercept(ApplicationCallPipeline.Monitoring) {
         if (context.request.uri.isInteresting()) {
            println("-----------------")
            println("-----------------")
            println("-----------------")
            println("-----------------")
            println("-----------------")
            Metrics.countUserAgent(context.request.header("User-Agent") ?: "unknown")
         }
      }

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
         Zip -> {
            call.response.header("Content-Disposition", ContentDisposition.Attachment.withParameter("filename", "${request.appName}.zip").toString())
            call.respondOutputStream(Zip, OK) { response.zipToStream(this) }
         }
         Json -> call.respond(response.b64EncodeValues())
         else -> call.respond(UnsupportedMediaType)
      }
      Metrics.countNewDownload(request.team, request.platform, requestedFormat.toString())
   }
}

fun parse(userAgent: String) = userAgent.lastIndexOf(' ').let { space ->
   val fromIdx = if (space != -1) space + 1 else 0
   userAgent.substring(fromIdx)
}

private fun String.isInteresting() = this.contains("/internal", false)



