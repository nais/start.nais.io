package io.nais.metrics

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.nais.request.PLATFORM
import io.prometheus.client.Counter

object Metrics {

   val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

   private val downloadsCounter = Counter.build()
      .name("apps")
      .labelNames("team", "platform")
      .help("Nr of generated zip files")
      .register(meterRegistry.prometheusRegistry)

   fun scrape(): String = meterRegistry.scrape()

   fun countNewDownload(team: String, platform: PLATFORM) {
      downloadsCounter.labels(team, platform.toString()).inc()
   }

}
