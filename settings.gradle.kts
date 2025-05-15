plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "Shulkerbox"
include("papermc")
include("common")
include("fabric")
include("dockyard")
include("minestom")