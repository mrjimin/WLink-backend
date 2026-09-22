package xyz.mrjimin.wlink.config

data class DatabaseConfig(
    val url: String = "jdbc:postgresql://${requiredEnv("DB_HOST")}:${requiredEnv("DB_PORT")}/${requiredEnv("DB_NAME")}",
    val user: String = requiredEnv("DB_USER"),
    val password: String = requiredEnv("DB_PASSWORD"),
    val driverClass: String = "org.postgresql.Driver",
) {
    companion object {
        private fun requiredEnv(name: String): String {
            return System.getenv(name)
                ?: error("$name environment variable is not set")
        }
    }
}