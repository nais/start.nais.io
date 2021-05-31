package io.nais

import com.google.cloud.functions.HttpFunction
import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.streams.asSequence

@Suppress("unused") // invoked by Google
@ExperimentalSerializationApi
class CloudFunction: HttpFunction {
   override fun service(googleRequest: HttpRequest, googleResponse: HttpResponse) {
      runCatching {
         val naisRequest = googleRequest.toNaisRequest()
         responseFrom(naisRequest)
      }.fold(
         { naisResponse ->
            if (googleRequest.isForZip()) {
               googleResponse.setContentType("application/zip")
               zipTo(googleResponse.outputStream, naisResponse)
            } else {
               // base64 encode yaml inside json to avoid trouble with quotes etc.
               val b64Values = naisResponse.mapValues { it.value.toBase64() }
               googleResponse.setContentType("application/json")
               googleResponse.writer.write(Json.encodeToString(b64Values))
            }
         },
         { throwable -> googleResponse?.setStatusCode(400, throwable.message) }
      )
   }
}

private fun HttpRequest.toNaisRequest():Request =
   reader.lines().asSequence().joinToString().let {
      Json{ignoreUnknownKeys = true}.decodeFromString(it)
   }

private fun HttpRequest.isForZip() =
   this.getFirstHeader("Accept")
      .map { it.equals("application/zip") }
      .orElse(false)

private fun String.toBase64() = Base64.getEncoder().encodeToString(this.encodeToByteArray())


