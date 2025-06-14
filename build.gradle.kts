plugins {
    kotlin("jvm") version "2.1.10"
}

group = "cz.lukynka"
version = "2.9"

subprojects {
    plugins.apply("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.register("publish-all") {
    dependsOn(tasks.getByName("build"))
    dependsOn(project(":common").tasks.getByName("publish"))
    dependsOn(project(":dockyard").tasks.getByName("publish"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}