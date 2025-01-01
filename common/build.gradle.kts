plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
}

group = "cz.lukynka"
version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    repositories {
        maven {
            url = if(version.toString().contains("-SNAPSHOT")) {
                uri("https://mvn.devos.one/snapshots")
            } else {
                uri("https://mvn.devos.one/releases")
            }
            credentials {
                username = System.getenv()["MAVEN_USER"]
                password = System.getenv()["MAVEN_PASS"]
            }
        }
    }
    publications {
        register<MavenPublication>("maven") {
            groupId = "cz.lukynka.shulkerbox"
            artifactId = "common"
            version = version
            from(components["java"])
        }
    }
}