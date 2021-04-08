package io.nais

import io.ktor.application.*
import io.ktor.application.Application
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.ContentType.*
import io.ktor.http.ContentType.Application.Zip
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.nais.mapping.*
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
      val yamlFiles = yamlFilesFrom(request)
      val requestedFormat = call.request.accept() ?: Text.Plain.toString()
      Metrics.countNewDownload(request.team, request.platform, requestedFormat)
      if (requestedFormat == Zip.toString()) {
         call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=${request.appName}.zip")
         call.respondOutputStream(Zip, OK) { yamlFiles.asZipStream(this) }
      } else {
         call.respond(yamlFiles.asJson())
      }
   }
}


