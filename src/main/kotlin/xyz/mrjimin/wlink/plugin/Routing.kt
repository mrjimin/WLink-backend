package xyz.mrjimin.wlink.plugin

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.request.host
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.mrjimin.wlink.api.keis.keisRoutes
import xyz.mrjimin.wlink.api.schedule.scheduleRoutes
import xyz.mrjimin.wlink.auth.googleOAuthRoutes

fun Application.configureRouting() {
    install(XForwardedHeaders) {
        useLastProxy()
    }

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        get("/ip") {
            val clientIp = call.request.origin.remoteHost

            call.respond(mapOf(
                "remote-host" to clientIp
            ))
        }

        googleOAuthRoutes()
        scheduleRoutes()
        keisRoutes()
    }
}
