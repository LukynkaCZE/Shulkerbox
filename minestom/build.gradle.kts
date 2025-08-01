plugins {
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.10"
}

group = "cz.lukynka"
version = parent!!.version

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom:2025.07.11-1.21.7")
    implementation("dev.hollowcube:schem:1.3.1")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:6.3.0.202209071007-r")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.akuleshov7:ktoml-core:0.5.1")
    implementation("com.akuleshov7:ktoml-file:0.5.1")
    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

sourceSets["main"].resources.srcDir("${buildDir}/generated/resources/")

sourceSets["main"].java.srcDir("src/main/kotlin")

tasks {
    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier = "sources"
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }
}

java {
    withSourcesJar()
    withJavadocJar()
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
            artifactId = "minestom"
            version = version
            from(components["java"])
        }
    }
}