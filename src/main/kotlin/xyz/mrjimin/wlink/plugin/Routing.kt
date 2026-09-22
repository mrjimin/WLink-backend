package xyz.mrjimin.wlink.plugin

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.mrjimin.wlink.schedule.scheduleRoutes

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        scheduleRoutes()
    }
}