package io.nais.e2e

import io.ktor.http.ContentType.*
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.*
import io.nais.mainModule
import io.nais.observabilityModule
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class E2ETest {

   @Test
   fun `root serves index html file`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Get, uri = "/")
         assertEquals(OK, call.response.status())
         assertTrue(call.response.content?.contains("<html") ?: false)
      }
   }

   @Test
   fun `static files are served`() {
      withTestApplication({ mainModule() }) {
         listOf(
            handleRequest(method = Get, uri = "/index.html"),
            handleRequest(method = Get, uri = "/style.css"),
            handleRequest(method = Get, uri = "/script.js")
         ).forEach {
            assertEquals(OK, it.response.status())
         }
      }
   }

   @Test
   fun `asking for zip yields a zipped response`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader(ContentType, Application.Json.toString())
            addHeader(Accept, Application.Zip.toString())
            setBody("""{"appName": "myeapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(OK, call.response.status())
         assertTrue(call.response.headers["Content-Type"] == Application.Zip.toString())
      }
   }

   @Test
   fun `asking for text yields a text response`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader(ContentType, "application/json")
            addHeader(Accept, Text.Plain.toString())
            addHeader(Accept, Text.Plain.toString())
            setBody("""{"appName": "myeapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(OK, call.response.status())
         assertTrue(call.response.headers["Content-Type"]?.contains(
            Text.Plain.toString()) ?: false
         )
      }
   }

   @Test
   fun `posting an invalid request yields a 400 with an explanation`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader(ContentType, "application/json")
            setBody("""{"team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(BadRequest, call.response.status())
         assertTrue(call.response.content?.contains("'appName' is required") ?: false)
      }
   }

   @Test
   fun `observability module responds to nais endpoints`() {
      withTestApplication({
         observabilityModule()
      }) {
         listOf(
            handleRequest(method = Get, uri = "/internal/isalive"),
            handleRequest(method = Get, uri = "/internal/isready"),
            handleRequest(method = Get, uri = "/internal/metrics")
         ).forEach { call ->
            assertEquals(OK, call.response.status())
         }
      }
   }

}
