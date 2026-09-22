package xyz.mrjimin.wlink.schedule

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

fun Route.scheduleRoutes() {
    val service by inject<ScheduleService>()

    route("/api/schedules") {
        get {
            call.respond(service.getSchedules())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.let {
                runCatching { Uuid.parse(it) }.getOrNull()
            } ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")

            val schedule = service.getSchedule(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Schedule not found")

            call.respond(schedule)
        }
    }
}