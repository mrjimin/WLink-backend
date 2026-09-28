package xyz.mrjimin.wlink.util

import java.time.LocalDate
import java.time.ZoneId

enum class Date {
    YEAR, MONTH, WEEK, TODAY
}

val KST: ZoneId = ZoneId.of("Asia/Seoul")
val NOW: LocalDate = LocalDate.now(KST)

val nowYear = NOW.year
val nowMonth = NOW.monthValue
val nowDay = NOW.dayOfMonth
