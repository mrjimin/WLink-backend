package xyz.mrjimin.wlink.plugin

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import xyz.mrjimin.wlink.schedule.scheduleModule

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
//            databaseModule,
            scheduleModule
        )
    }
}
