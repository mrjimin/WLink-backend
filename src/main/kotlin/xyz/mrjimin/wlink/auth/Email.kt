package xyz.mrjimin.wlink.auth

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable
@JvmInline
value class Email(val value: String) {

    init {
        if (!Pattern.matches("^[_a-zA-Z0-9-]+(.[_a-zA-Z0-9-]+)*@(?:\\w+\\.)+\\w+$", value)) {
            throw IllegalArgumentException("Invalid email format: $value")
        }
        if (domain != ALLOWED_DOMAIN) {
            throw IllegalArgumentException("Access Denied: Only '@${ALLOWED_DOMAIN}' domains are allowed. (Got: $domain)")
        }
    }

    val localPart: String
        get() = value.substringBefore('@')

    val domain: String
        get() = value.substringAfter('@')

    companion object {
            const val ALLOWED_DOMAIN = "gmail.com"
    }
}

fun String.toEmail(): Email = Email(this)
