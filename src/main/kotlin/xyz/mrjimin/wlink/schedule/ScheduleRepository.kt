package xyz.mrjimin.wlink.schedule

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.uuid.Uuid

object ScheduleTable : UuidTable("schedules") {
    val title = varchar("title", 255)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val isPeriod = bool("is_period")
    val createdAt = datetime("created_at")

    init {
        index(false, startDate, endDate)
    }
}

interface ScheduleRepository {

    fun findAll(): List<Schedule>

    fun findById(id: Uuid): Schedule?

    fun findByDate(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Schedule>
}