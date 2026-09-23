package xyz.mrjimin.wlink.schedule

import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

class ScheduleRepositoryImpl : ScheduleRepository {

    override fun findAll(): List<Schedule> {
        return transaction {
            ScheduleTable
                .selectAll()
                .orderBy(
                    ScheduleTable.startDate to SortOrder.ASC,
                    ScheduleTable.endDate to SortOrder.ASC,
                )
                .map(::toSchedule)
        }
    }

    override fun findById(id: Uuid): Schedule? {
        return transaction {
            ScheduleTable
                .selectAll()
                .where {
                    ScheduleTable.id eq id
                }
                .singleOrNull()
                ?.let(::toSchedule)
        }
    }

    override fun findByDate(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Schedule> {
        return transaction {
            ScheduleTable
                .selectAll()
                .where {
                    (ScheduleTable.startDate lessEq endDate) and
                            (ScheduleTable.endDate greaterEq startDate)
                }
                .orderBy(
                    ScheduleTable.startDate to SortOrder.ASC,
                    ScheduleTable.endDate to SortOrder.ASC,
                )
                .map(::toSchedule)
        }
    }

    private fun toSchedule(row: ResultRow): Schedule = Schedule(
        id = row[ScheduleTable.id].value,
        title = row[ScheduleTable.title],
        startDate = row[ScheduleTable.startDate],
        endDate = row[ScheduleTable.endDate],
        isPeriod = row[ScheduleTable.isPeriod],
        createdAt = row[ScheduleTable.createdAt]
    )
}