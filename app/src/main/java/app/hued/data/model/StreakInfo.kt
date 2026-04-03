package app.hued.data.model

import java.time.LocalDate

data class StreakInfo(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val toneFamily: String,
    val dayCount: Int,
    val isActive: Boolean,
)
