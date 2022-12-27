import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    application
}

group = "me.zukoap"
version = "1.0"
val jmhVersion = "1.32"
val ktorVersion ="1.6.7"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
    //logging
    implementation("io.github.microutils:kotlin-logging:2.1.16")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("ch.qos.logback:logback-core:1.2.8")
    //Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    //gson
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    //benchmarking
    implementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
    //redis
    implementation("redis.clients:jedis:3.7.1")
    //apache kafka
    implementation("org.apache.kafka:kafka-clients:3.0.0")
    implementation("com.google.guava:guava:31.0.1-jre")
}

tasks.test {
    useJUnit()
}

tasks.withType<Jar>(){
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("webServer/AppKt")
}
