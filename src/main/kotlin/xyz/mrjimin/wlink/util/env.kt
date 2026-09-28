package xyz.mrjimin.wlink.util

fun requiredEnv(name: String): String {
    return System.getenv(name.uppercase())
        ?: error("$name environment variable is not set")
}
