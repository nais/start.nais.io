package io.nais

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Zip
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.nais.deploy.serialize
import io.nais.mapping.appVarsFrom
import io.nais.mapping.gitHubWorkflowFrom
import io.nais.mapping.naisApplicationFrom
import io.nais.metrics.Metrics
import io.nais.naisapp.Environment
import io.nais.naisapp.serialize
import io.nais.request.Request
import io.nais.zip.zipTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import java.io.OutputStream
import java.nio.file.Paths

@ExperimentalSerializationApi
@Suppress("unused") // referenced in application.conf
fun Application.mainModule() {

   install(ContentNegotiation) {
      json(contentType = ContentType.Application.Json)
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
      Metrics.countNewDownload(request.team, request.platform)
      if (call.request.accept() ?: "" == Zip.toString()) {
         call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=${request.appName}.zip")
         call.respondOutputStream(Zip, OK) { zipIt(request, this) }
      } else {
         call.respondText(toText(request))
      }
   }
}


@ExperimentalSerializationApi
private fun toText(request: Request) = """
# nais.yaml
---
${naisApplicationFrom(request).serialize()}

# dev.yaml
---
${appVarsFrom(request, Environment.DEV).serialize()}

# prod.yaml
---
${appVarsFrom(request, Environment.PROD).serialize()}

# main-workflow.yaml
---
${gitHubWorkflowFrom(request).serialize()}
"""

@ExperimentalSerializationApi
private fun zipIt(request: Request, outputStream: OutputStream) =
   zipTo(outputStream, mapOf(
      Paths.get(".nais/nais.yaml") to naisApplicationFrom(request).serialize(),
      Paths.get(".nais/dev.yaml") to appVarsFrom(request, Environment.DEV).serialize(),
      Paths.get(".nais/prod.yaml") to appVarsFrom(request, Environment.PROD).serialize(),
      Paths.get(".github/workflows/main.yaml") to gitHubWorkflowFrom(request).serialize(),
   ))

