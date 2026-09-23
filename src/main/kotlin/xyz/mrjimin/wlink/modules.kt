package xyz.mrjimin.wlink

import io.ktor.server.application.*
import xyz.mrjimin.wlink.plugin.*

fun Application.module() {
    configureKoin()
    configureExposed()
    configureSerialization()
    configureHttp()
    configureSecurity()
    configureRouting()
}
