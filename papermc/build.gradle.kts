plugins {
    kotlin("jvm") version "2.1.10"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "cz.lukynka.shulkerbox.papermc"
version = parent!!.version

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo")
    maven("https://maven.pvphub.me/releases")
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven {
        name = "devOS"
        url = uri("https://mvn.devos.one/releases")
    }
}

dependencies {
    val scoreboardLibraryVersion = "2.4.1"
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-text-minimessage:4.24.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.incendo:cloud-core:2.0.0")
    implementation("org.incendo:cloud-kotlin-extensions:2.0.0")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.10")
    implementation("cz.lukynka:lkws:1.2")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.14")
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.14")
    implementation("net.megavex:scoreboard-library-api:$scoreboardLibraryVersion")
    runtimeOnly("net.megavex:scoreboard-library-implementation:$scoreboardLibraryVersion")
    implementation("net.megavex:scoreboard-library-extra-kotlin:$scoreboardLibraryVersion")
    runtimeOnly("net.megavex:scoreboard-library-modern:$scoreboardLibraryVersion:mojmap")
    implementation("com.akuleshov7:ktoml-core:0.5.1")
    implementation("com.akuleshov7:ktoml-file:0.5.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.3.0.202209071007-r")
    implementation("com.mattmx:ktgui:2.0")
    implementation("com.github.retrooper:packetevents-spigot:2.9.4")
    api(project(":common"))
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