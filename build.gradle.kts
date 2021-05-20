import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import se.patrikerdes.UseLatestVersionsTask

val logbackVersion = "1.3.0-alpha5"
val logstashEncoderVersion = "6.6"
val junitJupiterVersion = "5.8.0-M1"
val kotlinSerializationVersion = "1.2.0"
val kamlVersion = "0.31.0"

val mainClassName = "io.nais.MainKt"

plugins {
   kotlin("jvm") version "1.4.32"
   kotlin("plugin.serialization") version "1.4.32"
   id("se.patrikerdes.use-latest-versions") version "0.2.16"
   id("com.github.ben-manes.versions") version "0.38.0"
}

java {
   sourceCompatibility = JavaVersion.VERSION_16
   targetCompatibility = JavaVersion.VERSION_16
}

repositories {
   mavenCentral()
}

dependencies {
   implementation(kotlin("stdlib"))
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinSerializationVersion")
   implementation("com.charleskorn.kaml:kaml:$kamlVersion")

   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
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
         jvmTarget = "15"
      }
   }

   withType<Test> {
      useJUnitPlatform()
      testLogging {
         events("passed", "skipped", "failed")
         exceptionFormat = FULL
      }
   }

   withType<Wrapper> {
      gradleVersion = "7.0"
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
