package app.hued.data.model

import java.time.LocalDate

data class PaletteData(
    val id: Long,
    val periodType: TimePeriod,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val colors: List<ColorEntry>,
    val dominantColor: ColorEntry?,
    val poeticDescription: String,
    val photoCount: Int,
)
