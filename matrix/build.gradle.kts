import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    application
    kotlin("plugin.serialization") version "1.5.31"
    id("me.champeau.jmh") version "0.6.6"
}

group = "me.zukoap"
version = "1.0"
val jmhVersion = "1.32"


repositories {
    mavenCentral()
}

dependencies {
    //implementation("junit:junit:4.13.1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    //logging
    implementation("io.github.microutils:kotlin-logging:2.1.16")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("ch.qos.logback:logback-core:1.2.8")
    //gson
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    //benchmarking
    testImplementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
    //redis
    implementation("redis.clients:jedis:3.7.1")
    //apache kafka
    implementation("org.apache.kafka:kafka-clients:3.0.0")
    implementation("com.google.guava:guava:31.0.1-jre")

}
tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
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

application {
    mainClass.set("MainKt")
}

