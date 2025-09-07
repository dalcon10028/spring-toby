plugins {
    kotlin("jvm") version "2.2.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val springVersion = "6.2.10"

    implementation("org.springframework:spring-context:$springVersion")
    implementation("org.springframework:spring-jdbc:$springVersion")
    implementation("com.h2database:h2:2.3.232")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}