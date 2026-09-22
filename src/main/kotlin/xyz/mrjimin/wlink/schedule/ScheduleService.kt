package xyz.mrjimin.wlink.schedule

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class ScheduleService(
    private val repository: ScheduleRepository,
) {

    fun getSchedules(): List<Schedule> {
        return repository.findAll()
    }

    fun getSchedule(id: Uuid): Schedule? {
        return repository.findById(id)
    }

    fun getSchedulesByDate(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Schedule> {
        require(startDate <= endDate) {
            "endDate must be greater than or equal to startDate"
        }

        return repository.findByDate(startDate, endDate)
    }
}