package xyz.mrjimin.wlink.schedule

import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime

object ScheduleTable : UuidTable("schedules") {
    val title = varchar("title", 255)
    val startDate = date("start_date")
    val endDate = date("end_date")
    val isPeriod = bool("is_period")
    val createdAt = datetime("created_at")
}
