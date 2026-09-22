package xyz.mrjimin.wlink.schedule

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

interface ScheduleRepository {

    fun findAll(): List<Schedule>

    fun findById(id: Uuid): Schedule?

    fun findByDate(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Schedule>
}