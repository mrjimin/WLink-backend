package xyz.mrjimin.wlink.auth

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable
@JvmInline
value class Email(val value: String) {

    init {
        if (!Pattern.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$", value)) {
            throw IllegalAccessError()
        }
        if (domain != "mrjimin.xyz") throw IllegalAccessError()
    }

    val localPart: String
        get() = value.substringBefore('@')

    val domain: String
        get() = value.substringAfter('@')
}

fun String.toEmail(): Email = Email(this)
