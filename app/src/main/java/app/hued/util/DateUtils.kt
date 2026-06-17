package app.hued.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters

object DateUtils {

    // Weeks always start on Monday (ISO) regardless of device locale. A locale-dependent week start
    // (Sunday in the US, Monday in the EU) produced non-reproducible buckets that also disagreed with
    // the Monday weekly-notification cadence.
    fun startOfWeek(date: LocalDate = LocalDate.now()): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    // Exclusive upper bound: the start of the following week. Period queries are half-open
    // (timestamp < endTimestamp), so this never overlaps the next week.
    fun endOfWeek(date: LocalDate = LocalDate.now()): LocalDate =
        startOfWeek(date).plusDays(7)

    fun startOfMonth(date: LocalDate = LocalDate.now()): LocalDate =
        date.withDayOfMonth(1)

    fun endOfMonth(date: LocalDate = LocalDate.now()): LocalDate =
        date.withDayOfMonth(1).plusMonths(1)

    fun startOfSeason(date: LocalDate = LocalDate.now()): LocalDate {
        val seasonStart = when (date.month) {
            Month.DECEMBER, Month.JANUARY, Month.FEBRUARY -> Month.DECEMBER
            Month.MARCH, Month.APRIL, Month.MAY -> Month.MARCH
            Month.JUNE, Month.JULY, Month.AUGUST -> Month.JUNE
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER -> Month.SEPTEMBER
        }
        val year = if (date.month == Month.DECEMBER) date.year else {
            if (seasonStart == Month.DECEMBER) date.year - 1 else date.year
        }
        return LocalDate.of(year, seasonStart, 1)
    }

    fun endOfSeason(date: LocalDate = LocalDate.now()): LocalDate =
        startOfSeason(date).plusMonths(3)

    fun startOfYear(date: LocalDate = LocalDate.now()): LocalDate =
        LocalDate.of(date.year, 1, 1)

    fun endOfYear(date: LocalDate = LocalDate.now()): LocalDate =
        LocalDate.of(date.year + 1, 1, 1)

    fun formatMonth(date: LocalDate): String =
        "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"

    fun formatWeek(startDate: LocalDate): String {
        val end = startDate.plusDays(6)
        val startMonth = startDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val endMonth = end.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return if (startDate.month == end.month) {
            "${startDate.dayOfMonth}–${end.dayOfMonth} $startMonth ${end.year}"
        } else {
            "${startDate.dayOfMonth} $startMonth – ${end.dayOfMonth} $endMonth ${end.year}"
        }
    }
}
