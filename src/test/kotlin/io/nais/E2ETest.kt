package io.nais

import com.charleskorn.kaml.Yaml
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType.*
import io.ktor.http.HttpHeaders.Accept
import io.ktor.http.HttpHeaders.ContentType
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
import kotlinx.serialization.json.jsonObject

@ExperimentalSerializationApi
class E2ETest {

   @Test
   fun `root serves index html file`() {
      testApplication() {
         val response = client.get("/")
         assertEquals(OK, response.status)
         assertTrue(response.bodyAsText().contains("<html"))
      }
   }

   @Test
   fun `static files are served`() {
      testApplication() {
         listOf(
            client.get("/index.html"),
            client.get("/style.css"),
            client.get("/script.js")
         ).forEach { response ->
            assertEquals(OK, response.status)
         }
      }
   }

   @Test
   fun `asking for zip yields a zipped response`() {
      testApplication() {
         val response = client.post("/app") {
            header(ContentType, Application.Json.toString())
            header(Accept, Application.Zip.toString())
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(OK, response.status)
         assertTrue(response.headers["Content-Type"] == Application.Zip.toString())
         assertTrue(response.headers["Content-Disposition"]?.contains("filename") ?: false)
      }
   }

   @Test
   fun `asking for json yields a json response`() {
      testApplication() {
         val response = client.post("/app") {
            header(ContentType, Application.Json.toString())
            header(Accept, Application.Json.toString())
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(OK, response.status)
         assertTrue(response.headers["Content-Type"]?.contains(
            Application.Json.toString()) ?: false
         )
      }
   }

   @Test
   fun `asking for other content types yields a 415`() {
      testApplication() {
         val response = client.post("/app") {
            header(ContentType, "image/png")
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(UnsupportedMediaType, response.status)
      }
   }

   @Test
   fun `json values contain yaml and are b64 encoded to avoid messed up chars`() {
      testApplication() {
         val response = client.post("/app") {
            header(ContentType, Application.Json.toString())
            header(Accept, Application.Json.toString())
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         val responseJson = Json.decodeFromString(JsonObject.serializer(), response.bodyAsText())
         val ghWorkflow = decode(responseJson[".github/workflows/main.yaml"].toString())
         val yaml = Yaml.default.decodeFromString(GitHubWorkflow.serializer(), ghWorkflow)
         assertNotNull(yaml)
      }
   }

   @Test
   fun `posting an invalid request yields a 400`() {
      testApplication() {
         val response = client.post("/app") {
            header(ContentType, "application/json")
            setBody("""{"team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         assertEquals(BadRequest, response.status)
         assertTrue(response.bodyAsText().contains("Unable to parse JSON"))
      }
   }

   @Test
   fun `observability module responds to nais endpoints`() {
      testApplication() {
         listOf(
            client.get("/internal/isalive"),
            client.get("/internal/isready"),
            client.get("/internal/metrics")
         ).forEach { response ->
            assertEquals(OK, response.status)
         }
      }
   }

   @Test
   fun `dockerfile is added`() {
      testApplication() {
         val response = client.post("/app") {
            header(ContentType, Application.Json)
            header(Accept, Application.Json)
            setBody("""{"appName": "myapp", "team": "myteam", "platform": "JVM_GRADLE", "extras": []}""")
         }
         val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
         assertNotNull(body["Dockerfile"])
      }
   }

   private fun decode(encoded: String) =
      String(Base64.getDecoder().decode(encoded.replace("\"", "")))

}
