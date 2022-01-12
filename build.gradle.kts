import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import se.patrikerdes.UseLatestVersionsTask

val ktorVersion = "1.6.5"
val logbackVersion = "1.3.0-alpha12"
val logstashEncoderVersion = "7.0.1"
val junitJupiterVersion = "5.8.2"
val kotlinSerializationVersion = "1.3.2"
val kamlVersion = "0.38.0"
val micrometerVersion = "1.8.1"

val mainClassName = "io.nais.MainKt"

plugins {
   kotlin("jvm") version "1.6.10"
   kotlin("plugin.serialization") version "1.6.10"
   id("com.github.johnrengelman.shadow") version "7.1.2"
   id("se.patrikerdes.use-latest-versions") version "0.2.18"
   id("com.github.ben-manes.versions") version "0.40.0"
}

java {
   sourceCompatibility = JavaVersion.VERSION_17
   targetCompatibility = JavaVersion.VERSION_17
}

repositories {
   mavenCentral()
}

dependencies {
   implementation(kotlin("stdlib"))
   implementation("io.ktor:ktor-server-core:$ktorVersion")
   implementation("io.ktor:ktor-server-netty:$ktorVersion")
   implementation("io.ktor:ktor-serialization:$ktorVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinSerializationVersion")
   implementation("com.charleskorn.kaml:kaml:$kamlVersion")
   implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
   implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
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

   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions {
         jvmTarget = "17"
      }
   }

   withType<Test> {
      useJUnitPlatform()
      testLogging {
         events(FAILED)
         showExceptions = true
         showStandardStreams = true
         exceptionFormat = FULL
      }
   }

   withType<Wrapper> {
      gradleVersion = "7.3"
   }

   named("useLatestVersions", UseLatestVersionsTask::class.java).configure {
      updateBlacklist = emptyList()
   }

}

fun isNonStable(version: String): Boolean {
   val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
   val regex = "^[0-9,.v-]+(-r)?$".toRegex()
   return (stableKeyword || regex.matches(version)).not()
}
