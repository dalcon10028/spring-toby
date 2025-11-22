plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.21"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val springVersion = "6.2.10"
    val kotestVersion = "6.0.0"
    val mockkVersion = "1.14.6"
    val kotlinLoggingVersion = "7.0.3"
    val aspectjVersion = "1.9.25"
    val coroutinesVersion = "1.10.2"

    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    // Spring
    implementation("org.springframework:spring-context:$springVersion")
    implementation("org.springframework:spring-jdbc:$springVersion")
    implementation("org.springframework:spring-aop:$springVersion")
    implementation("org.aspectj:aspectjweaver:$aspectjVersion")

    implementation("com.h2database:h2:2.3.232")
    implementation("org.springframework.integration:spring-integration-mail:7.0.0-M3")
    implementation("javax.mail:mail:1.4")
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework:spring-test:$springVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring:$kotestVersion")
    testImplementation("io.mockk:mockk:${mockkVersion}")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}