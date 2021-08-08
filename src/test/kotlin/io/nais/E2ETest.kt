package io.nais

import com.charleskorn.kaml.Yaml
import io.ktor.http.ContentType.*
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnsupportedMediaType
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

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
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(OK, call.response.status())
         assertTrue(call.response.headers["Content-Type"] == Application.Zip.toString())
         assertTrue(call.response.headers["Content-Disposition"]?.contains("filename") ?: false)
      }
   }

   @Test
   fun `asking for json yields a json response`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader(ContentType, Application.Json.toString())
            addHeader(Accept, Application.Json.toString())
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(OK, call.response.status())
         assertTrue(call.response.headers["Content-Type"]?.contains(
            Application.Json.toString()) ?: false
         )
      }
   }

   @Test
   fun `asking for other content types yields a 415`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader(ContentType, "image/png")
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(UnsupportedMediaType, call.response.status())
      }
   }

   @Test
   fun `json values contain yaml and are b64 encoded to avoid messed up chars`() {
      withTestApplication({ mainModule() }) {
         val call = handleRequest(method = Post, uri = "/app") {
            addHeader(ContentType, Application.Json.toString())
            addHeader(Accept, Application.Json.toString())
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         val responseJson = Json.decodeFromString(JsonObject.serializer(), call.response.content ?: "")
         val ghWorkflow = decode(responseJson[".github/workflows/main.yaml"].toString())
         val yaml = Yaml.default.decodeFromString(GitHubWorkflow.serializer(), ghWorkflow)
         assertNotNull(yaml)
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

   private fun decode(encoded: String) =
      String(Base64.getDecoder().decode(encoded.replace("\"", "")))

}
