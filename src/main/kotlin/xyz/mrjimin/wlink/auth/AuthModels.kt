package xyz.mrjimin.wlink.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val state: String,
    val token: String,
)

@Serializable
data class UserInfo(
    val id: String?,
    val email: Email?,
    @SerialName("verified_email") val verifiedEmail: Boolean,
    val name: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String,
    val locale: String,
    val hd: String
)

/*
data class UserInfo(
    val id: String?,
    val email: Email?,
    @SerialName("verified_email") val verifiedEmail: Boolean? = null,
    val name: String?,
    @SerialName("given_name") val givenName: String?,
    @SerialName("family_name") val familyName: String? = null,
    val picture: String?,
    val locale: String? = null,
    val hd: String? = null
)
*/
