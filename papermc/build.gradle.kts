plugins {
    kotlin("jvm") version "1.9.22"
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "cz.lukynka.shulkerbox.papermc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo")
    maven {
        name = "devOS"
        url = uri("https://mvn.devos.one/releases")
    }
}

dependencies {
    paperDevBundle("1.21-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.incendo:cloud-core:2.0.0-rc.2")
    implementation("org.incendo:cloud-kotlin-extensions:2.0.0-rc.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.8")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.8")
    implementation("cz.lukynka:lkws:1.2")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.9")

//    implementation(project(":common"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}