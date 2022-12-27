import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    application
    kotlin("plugin.serialization") version "1.5.31"
}

group = "me.zukoap"
version = "1.0"
val ktorVersion = "1.6.7"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    //ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    //logging
    implementation("io.github.microutils:kotlin-logging:2.1.16")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("ch.qos.logback:logback-core:1.2.8")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}