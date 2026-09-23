package xyz.mrjimin.wlink.plugin

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.collections.*
import xyz.mrjimin.wlink.auth.GoogleOAuthConfig
import xyz.mrjimin.wlink.auth.UserSession
import xyz.mrjimin.wlink.config.applicationHttpClient

val googleRedirects = ConcurrentMap<String, String>()

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<UserSession>("user_session")
    }

    install(Authentication) {
        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            settings = OAuthServerSettings.OAuth2ServerSettings(
                name = "google",
                authorizeUrl = GoogleOAuthConfig.AUTHORIZE_URL,
                accessTokenUrl = GoogleOAuthConfig.TOKEN_URL,
                requestMethod = HttpMethod.Post,
                clientId = GoogleOAuthConfig.clientId,
                clientSecret = GoogleOAuthConfig.clientSecret,
                defaultScopes = listOf(GoogleOAuthConfig.PROFILE_SCOPE),
                extraAuthParameters = listOf("access_type" to "offline"),
                onStateCreated = { call, state ->
                    call.request.queryParameters["redirectUrl"]?.let {
                        googleRedirects[state] = it
                    }
                }
            )
            fallback = { cause ->
                if (cause is OAuth2RedirectError) {
                    respondRedirect("/login-after-fallback")
                } else {
                    respond(HttpStatusCode.Forbidden, cause.message)
                }
            }
            client = applicationHttpClient
        }
    }
}
