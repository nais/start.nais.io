package io.nais

import io.ktor.server.application.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics

@Suppress("unused") // referenced in application.conf
fun Application.observabilityModule() {

   install(MicrometerMetrics) {
      registry = Metrics.meterRegistry

      meterBinders = listOf(
         ClassLoaderMetrics(),
         JvmMemoryMetrics(),
         JvmGcMetrics(),
         ProcessorMetrics(),
         JvmThreadMetrics()
      )
   }

   routing {
      observability()
   }
}

fun Route.observability() {
   get("/internal/isalive") {
      call.respond(OK)
   }

   get("/internal/isready") {
      call.respond(OK)
   }

   get("/internal/metrics") {
      call.respond(Metrics.scrape())
   }
}
