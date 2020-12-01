val ktorVersion = "1.4.2"
val logbackVersion = "1.2.3"
val logstashEncoderVersion = "6.5"
val junitJupiterVersion = "5.7.0"

val mainClassKt = "io.nais.AppKt"

plugins {
   kotlin("jvm") version "1.4.20"
   application
   id("com.github.johnrengelman.shadow") version "6.1.0"
}

java {
   sourceCompatibility = JavaVersion.VERSION_15
   targetCompatibility = JavaVersion.VERSION_15
}

application {
   mainClass.set("org.gradle.sample.Main")
}

repositories {
   mavenCentral()
   jcenter()
}

dependencies {
   implementation(kotlin("stdlib"))
   implementation("io.ktor:ktor-server-netty-kotlinMultiplatform:$ktorVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks {
   withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
      archiveBaseName.set("app")
      archiveClassifier.set("")
      manifest {
         attributes(
            mapOf(
               "Main-Class" to mainClassKt
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
