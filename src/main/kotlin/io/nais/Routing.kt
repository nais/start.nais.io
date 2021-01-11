package io.nais

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.nais.deploy.asYaml
import io.nais.mapping.appVarsFrom
import io.nais.mapping.gitHubWorkflowFrom
import io.nais.mapping.naisApplicationFrom
import io.nais.naisapp.Environment
import io.nais.naisapp.asYaml
import io.nais.request.Request
import io.nais.zip.zipTo
import kotlinx.serialization.ExperimentalSerializationApi
import java.nio.file.Paths

@ExperimentalSerializationApi
fun Route.app() {
   post("/app") {
      val request = call.receive<Request>()
      call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=${request.appName}.zip")
      call.respondOutputStream(ContentType.Application.Zip, HttpStatusCode.OK) {
         zipTo(this, mapOf(
            Paths.get(".nais/nais.yaml") to naisApplicationFrom(request).asYaml(),
            Paths.get(".nais/dev.yaml") to appVarsFrom(request, Environment.DEV).asYaml(),
            Paths.get(".nais/prod.yaml") to appVarsFrom(request, Environment.PROD).asYaml(),
            Paths.get(".github/workflows/main.yaml") to gitHubWorkflowFrom(request).asYaml(),
         ))
      }
   }
}
