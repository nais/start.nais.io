val ktorVersion = "2.2.1"
val logbackVersion = "1.4.5"
val logstashEncoderVersion = "7.2"
val junitJupiterVersion = "5.9.1"
val kamlVersion = "0.49.0"
val micrometerVersion = "1.10.2"

val mainClassName = "io.nais.MainKt"

plugins {
   kotlin("jvm") version "1.7.21"
   kotlin("plugin.serialization") version "1.7.21"
   id("com.github.johnrengelman.shadow") version "7.1.2"
   id("com.github.ben-manes.versions") version "0.42.0"
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
         jvmTarget = "11"
      }
   }

   withType<Test> {
      useJUnitPlatform()
      testLogging {
         showExceptions = true
      }
   }

   withType<Wrapper> {
      gradleVersion = "7.6"
   }

}
