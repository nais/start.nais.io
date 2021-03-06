import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

val ktorVersion = "1.5.0"
val logbackVersion = "1.2.3"
val logstashEncoderVersion = "6.5"
val mustacheVersion = "0.9.7"
val junitJupiterVersion = "5.7.0"
val kotlinSerializationVersion = "1.0.1"
val kamlVersion = "0.26.0"
val micrometerVersion = "1.6.2"

val mainClassName = "io.nais.MainKt"

plugins {
   kotlin("jvm") version "1.4.30"
   kotlin("plugin.serialization") version "1.4.30"
}

java {
   sourceCompatibility = JavaVersion.VERSION_15
   targetCompatibility = JavaVersion.VERSION_15
}

repositories {
   mavenCentral()
   jcenter()
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
      gradleVersion = "6.7.1"
   }

}
