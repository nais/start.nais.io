package io.nais

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Zip
import io.ktor.http.HttpHeaders.ContentDisposition
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.nais.deploy.asYaml
import io.nais.mapping.appVarsFrom
import io.nais.mapping.gitHubWorkflowFrom
import io.nais.mapping.naisApplicationFrom
import io.nais.naisapp.Environment
import io.nais.naisapp.Environment.DEV
import io.nais.naisapp.Environment.PROD
import io.nais.request.Request
import io.nais.naisapp.asYaml
import io.nais.zip.zipTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import java.nio.file.Paths

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
      post("/app") {
         val request = call.receive<Request>()
         call.response.header(ContentDisposition, "attachment; filename=${request.appName}.zip")
         call.respondOutputStream(Zip, OK) {
            zipTo(this, mapOf(
               Paths.get(".nais/nais.yaml") to naisApplicationFrom(request).asYaml(),
               Paths.get(".nais/dev.yaml") to appVarsFrom(request, DEV).asYaml(),
               Paths.get(".nais/prod.yaml") to appVarsFrom(request, PROD).asYaml(),
               Paths.get(".github/workflows/main.yaml") to gitHubWorkflowFrom(request).asYaml(),
            ))
         }
      }
   }
}
