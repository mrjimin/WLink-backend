package xyz.mrjimin.wlink.auth

object GoogleOAuthConfig {
    const val AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth"
    const val TOKEN_URL = "https://accounts.google.com/o/oauth2/token"
    val PROFILE_SCOPE = listOf(
        "https://www.googleapis.com/auth/userinfo.profile",
        "https://www.googleapis.com/auth/userinfo.email"
    )
    const val USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"

    val clientId get() = System.getenv("GOOGLE_CLIENT_ID").orEmpty()
    val clientSecret get() = System.getenv("GOOGLE_CLIENT_SECRET").orEmpty()
}
