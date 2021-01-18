package io.nais.e2e

import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Found
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
   fun `root redirects to index`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Get, uri = "/")
         assertEquals(Found, call.response.status())
         assertEquals("/index.html", call.response.headers["Location"])
      }
   }

   @Test
   fun `index is served from static resources`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Get, uri = "/index.html")
         assertEquals(OK, call.response.status())
         assertTrue(call.response.content?.contains("<html") ?: false)
      }
   }

   @Test
   fun `posting a valid request yields a zipped response`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader("Content-Type", "application/json")
            setBody("""{"appName": "myeapp", "team": "myteam", "platform": "JVM_GRADLE"}""")
         }
         assertEquals(OK, call.response.status())
         assertTrue(call.response.headers["Content-Type"] == "application/zip")
      }
   }

   @Test
   fun `posting an invalid request yields a 400 with an explanation`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader("Content-Type", "application/json")
            setBody("""{"team": "myteam", "platform": "JVM_GRADLE"}""")
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
         val aliveCall = handleRequest(method = Get, uri = "/internal/isalive")
         val readyCall = handleRequest(method = Get, uri = "/internal/isready")
         val metricsCall = handleRequest(method = Get, uri = "/internal/metrics")
         assertEquals(OK, aliveCall.response.status())
         assertEquals(OK, readyCall.response.status())
         assertEquals(OK, metricsCall.response.status())
      }
   }

}
