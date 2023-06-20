plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

tasks.withType<Test> {
    minHeapSize = "512m"
    maxHeapSize = "1024m"
    jvmArgs = listOf("-XX:MaxPermSize=512m")
}
