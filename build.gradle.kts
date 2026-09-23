plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

group = "xyz.mrjimin"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.requestValidation)
    implementation(libs.koin.ktor)
    implementation(libs.koin.loggerSlf4j)
    implementation(libs.logback.classic)

    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.cio)
    implementation(ktorLibs.client.contentNegotiation)

    implementation(exposedLibs.core)
    implementation(exposedLibs.jdbc)
    implementation(exposedLibs.kotlin.datetime)
//    implementation(exposedLibs.java.time)
//    implementation(libs.h2database.h2)
    implementation(libs.postgresql)
//    implementation(libs.ksoup)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
