package xyz.mrjimin.wlink.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.uri
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import xyz.mrjimin.wlink.config.applicationHttpClient
import xyz.mrjimin.wlink.plugin.googleRedirects
import java.util.concurrent.ConcurrentHashMap

private val userInfoCache = ConcurrentHashMap<String, UserInfo>()

fun Route.googleOAuthRoutes() {
    authenticate("auth-oauth-google") {
        get("/login") {}

        get("/callback") {
            val currentPrincipal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                ?: return@get call.respondText("Google Authentication Failed", status = HttpStatusCode.Unauthorized)

            val state = currentPrincipal.state
            val session = UserSession(state ?: "unknown", currentPrincipal.accessToken)
            call.sessions.set(session)

            if (state != null) {
                googleRedirects.remove(state)?.let { url ->
                    call.respondRedirect(url)
                    return@get
                }
            }

            call.respondRedirect("/home/google")
        }
    }

    get("/home/google") {
        val userSession = call.sessions.get<UserSession>() ?: run {
            call.respondRedirect("/")
            return@get
        }

        val userInfo = getPersonalGreeting(applicationHttpClient, userSession)
        call.respond(userInfo)
    }

    get("/home") {
        val userSession = getSession(call) ?: return@get
        val userInfo = getPersonalGreeting(applicationHttpClient, userSession)
        call.respondText("Hello, ${userInfo.name}! Welcome home!")
    }

    get("/login-after-fallback") {
        call.respondText("Redirected after fallback")
    }
}

suspend fun getPersonalGreeting(httpClient: HttpClient, userSession: UserSession): UserInfo {
    userInfoCache[userSession.token]?.let { return it }

    val userInfo: UserInfo = httpClient.get(GoogleOAuthConfig.USER_INFO_URL) {
        headers {
            append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
        }
    }.body()

    userInfoCache[userSession.token] = userInfo
    return userInfo
}

suspend fun getSession(call: ApplicationCall): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    if (userSession == null) {
        val loginUrl = "http://localhost:8080/login"
        val redirectUrl = URLBuilder(loginUrl).run {
            parameters.append("redirectUrl", call.request.uri)
            build()
        }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}
