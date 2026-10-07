package xyz.mrjimin.wlink.plugin

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.origin
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

        get("/ip") {
            install(ForwardedHeaders)

            val clientIp = call.request.origin.remoteHost
            call.respond(clientIp)
        }

        googleOAuthRoutes()
        scheduleRoutes()
        keisRoutes()
    }
}
