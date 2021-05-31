import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import se.patrikerdes.UseLatestVersionsTask

val junitJupiterVersion = "5.8.0-M1"
val kotlinSerializationVersion = "1.2.1"
val kamlVersion = "0.33.0"

val invoker by configurations.creating

plugins {
   kotlin("jvm") version "1.5.10"
   kotlin("plugin.serialization") version "1.5.10"
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

configurations { invoker }

dependencies {
   implementation(kotlin("stdlib"))
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
   implementation("com.charleskorn.kaml:kaml:$kamlVersion")

   compileOnly("com.google.cloud.functions:functions-framework-api:1.0.4")
   compileOnly("com.google.cloud:google-cloud-logging-logback:0.120.8-alpha")
   invoker("com.google.cloud.functions.invoker:java-function-invoker:1.0.0-alpha-2-rc5")

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
      gradleVersion = "7.0.2"
   }

   named("useLatestVersions", UseLatestVersionsTask::class.java).configure {
      updateBlacklist = emptyList()
   }
}

task<JavaExec>("runFunction") {
   main = "com.google.cloud.functions.invoker.runner.Invoker"
   classpath(invoker)
   inputs.files(configurations.runtimeClasspath, sourceSets["main"].output)
   args(
      "--target", project.findProperty("runFunction.target") ?: "io.nais.CloudFunction",
      "--port", project.findProperty("runFunction.port") ?: 8080
   )
   doFirst {
      args("--classpath", files(configurations.runtimeClasspath, sourceSets["main"].output).asPath)
   }
}

fun isNonStable(version: String): Boolean {
   val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
   val regex = "^[0-9,.v-]+(-r)?$".toRegex()
   return (stableKeyword || regex.matches(version)).not()
}
