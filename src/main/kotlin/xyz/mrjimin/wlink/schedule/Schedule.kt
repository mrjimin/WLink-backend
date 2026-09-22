package xyz.mrjimin.wlink.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Schedule(
    val id: Uuid,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isPeriod: Boolean,
    val createdAt: LocalDateTime,
)
