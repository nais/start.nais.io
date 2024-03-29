package io.nais

import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Application.Zip
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnsupportedMediaType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
@Suppress("unused") // referenced in application.conf
fun Application.mainModule() {

   install(ContentNegotiation) {
      json()
   }

   install(StatusPages) {
      exception<NullPointerException> { call, cause ->
         // Most likely because the body is empty
         call.respond(BadRequest, "Unable to parse request body: ${cause.message ?: "unknown or missing data"}")
      }

      exception<BadRequestException> { call, cause ->
         call.respond(BadRequest, "Unable to handle request: ${cause.message}")
      }

      exception<BadContentTypeFormatException> { call, cause ->
         call.respond(UnsupportedMediaType, cause.message ?: "Don't know how to serve ${call.request.contentType()}")
      }

      exception<Throwable> { call, cause ->
         call.respond(InternalServerError, cause.message ?: "Our bad, we screwed something up")
      }
   }

   routing {
      intercept(ApplicationCallPipeline.Monitoring) {
         if (context.request.uri.isInteresting()) {
            Metrics.countUserAgent(context.request.header("User-Agent") ?: "unknown")
         }
      }

      staticResources("/", "web")

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



