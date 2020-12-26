package io.nais.mapping

import io.nais.request.Request
import io.nais.response.Metadata
import io.nais.response.NaisApplication
import io.nais.response.Spec
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
fun naisApplicationFrom(req: Request) = NaisApplication(
   metadata = Metadata(
      name = req.appName,
      namespace = req.team,
      labels = mapOf("team" to req.team)
   ),
   spec = Spec(
      image = req.image
   )
)
