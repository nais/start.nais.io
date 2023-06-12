import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.cyclonedx.gradle.CycloneDxTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

val ktorVersion = "2.3.1"
val logbackVersion = "1.4.7"
val logstashEncoderVersion = "7.3"
val junitJupiterVersion = "5.9.3"
val kamlVersion = "0.54.0"
val micrometerVersion = "1.11.0"

val mainClassName = "io.nais.MainKt"

group = "io.nais"
version = "generatedlater"

plugins {
   kotlin("jvm") version "1.8.21"
   kotlin("plugin.serialization") version "1.8.21"
   id("com.github.johnrengelman.shadow") version "8.1.1"
   id("com.github.ben-manes.versions") version "0.47.0"
   id("org.cyclonedx.bom") version "1.7.4"
}

repositories {
   mavenCentral()
}

dependencies {
   implementation(kotlin("stdlib"))
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("com.charleskorn.kaml:kaml:$kamlVersion")
   implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
   implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
   implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
   implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
   implementation("io.ktor:ktor-server-core:$ktorVersion")
   implementation("io.ktor:ktor-server-netty:$ktorVersion")
   implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")

   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

kotlin {
   jvmToolchain(17)
}

tasks {
   withType<Jar> {
      archiveBaseName.set("app")

      manifest {
         attributes["Main-Class"] = "io.nais.MainKt"
         attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
         }
      }

      doLast {
         configurations.runtimeClasspath.get().forEach {
            val file = File("$buildDir/libs/${it.name}")
            if (!file.exists())
               it.copyTo(file)
         }
      }
   }

   withType<ShadowJar>{
      archiveFileName.set("app-all.jar")
   }

   withType<Test> {
      useJUnitPlatform()
      testLogging {
         showExceptions = true
      }
      testLogging {
         exceptionFormat = FULL
      }
   }

   withType<Wrapper> {
      gradleVersion = "8.1"
   }

   withType<CycloneDxTask> {
      setOutputFormat("json")
      setIncludeLicenseText(false)
   }

}

