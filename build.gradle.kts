plugins {
    kotlin("jvm") version "1.9.22"
}

group = "cz.lukynka"
version = "0.9-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}