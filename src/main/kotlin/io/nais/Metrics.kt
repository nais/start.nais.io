package io.nais

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.Counter

object Metrics {

   val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

   private val downloadsCounter = Counter.build()
      .name("apps")
      .labelNames("team", "platform", "format")
      .help("Nr of generated responses")
      .register(meterRegistry.prometheusRegistry)

   private val userAgentCounter = Counter.build()
      .name("useragent")
      .labelNames("name")
      .help("Nr of unique user agents")
      .register(meterRegistry.prometheusRegistry)

   fun scrape(): String = meterRegistry.scrape()

   fun countNewDownload(team: String, platform: PLATFORM, format: String) =
      downloadsCounter.labels(team, platform.toString(), format).inc()

   fun countUserAgent(name: String) =
      userAgentCounter.labels(name).inc()

}
