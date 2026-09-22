pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
    versionCatalogs {
        create("ktorLibs").from("io.ktor:ktor-version-catalog:3.6.0")
        create("exposedLibs").from("org.jetbrains.exposed:exposed-version-catalog:1.5.0")
    }
}

rootProject.name = "WLink-backend"

