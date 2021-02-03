package io.nais.metrics

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.nais.request.PLATFORM
import io.prometheus.client.Counter

object Metrics {

   val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

   private val downloadsCounter = Counter.build()
      .name("apps")
      .labelNames("team", "platform", "format")
      .help("Nr of generated responses")
      .register(meterRegistry.prometheusRegistry)

   fun scrape(): String = meterRegistry.scrape()

   fun countNewDownload(team: String, platform: PLATFORM, format: String) =
      downloadsCounter.labels(team, platform.toString(), format).inc()

}
