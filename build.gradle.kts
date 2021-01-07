val ktorVersion = "1.4.2"
val logbackVersion = "1.2.3"
val logstashEncoderVersion = "6.5"
val mustacheVersion = "0.9.7"
val junitJupiterVersion = "5.7.0"
val kotlinSerializationVersion = "1.0.1"
val kamlVersion = "0.26.0"
val micrometerVersion = "1.6.2"

val mainClassName = "io.nais.MainKt"

plugins {
   kotlin("jvm") version "1.4.21"
   id("com.github.johnrengelman.shadow") version "6.1.0"
   kotlin("plugin.serialization") version "1.4.21"
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
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks {
   withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
      archiveBaseName.set("app")
      manifest {
         attributes(
            mapOf(
               "Main-Class" to mainClassName
            )
         )
      }
   }

   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions {
         jvmTarget = "15"
      }
   }

   withType<Test> {
      useJUnitPlatform()
   }

   withType<Wrapper> {
      gradleVersion = "6.7.1"
   }

   "build" {
      dependsOn("shadowJar")
   }
}
