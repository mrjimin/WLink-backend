package xyz.mrjimin.wlink.plugin

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.mrjimin.wlink.api.keis.keisRoutes
import xyz.mrjimin.wlink.api.schedule.scheduleRoutes
import xyz.mrjimin.wlink.auth.googleOAuthRoutes

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        googleOAuthRoutes()
        scheduleRoutes()
        keisRoutes()
    }
}
