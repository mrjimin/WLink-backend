package xyz.mrjimin.wlink.plugin

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.mrjimin.wlink.config.DatabaseConfig
import xyz.mrjimin.wlink.schedule.ScheduleTable

fun Application.configureExposed() {
    val config = DatabaseConfig()

    Database.connect(
        url = config.url,
        user = config.user,
        password = config.password,
        driver = config.driverClass
    )

    transaction {
        SchemaUtils.create(ScheduleTable)
    }
}