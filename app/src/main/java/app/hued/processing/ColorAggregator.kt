package app.hued.processing

import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.model.TimePeriod
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

class ColorAggregator @Inject constructor(
    private val colorNamer: ColorNamer,
    private val poeticDescriptionMatcher: PoeticDescriptionMatcher,
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun aggregate(
        results: List<PaletteResultEntity>,
        periodType: TimePeriod,
        startDate: LocalDate,
        endDate: LocalDate,
        maxColors: Int = 5,
    ): PeriodPaletteEntity {
        val allColors = results.flatMap { result ->
            json.decodeFromString<List<String>>(result.colors)
        }

        val dominantColors = allColors
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(maxColors)
            .map { it.key }

        val dominant = dominantColors.firstOrNull() ?: "#808080"
        val poeticDescription = poeticDescriptionMatcher.match(dominantColors)

        return PeriodPaletteEntity(
            periodType = periodType.name,
            startDate = startDate.toEpochDay(),
            endDate = endDate.toEpochDay(),
            colors = json.encodeToString(ListSerializer(String.serializer()), dominantColors),
            photoCount = results.size,
            dominantColor = dominant,
            poeticDescription = poeticDescription,
        )
    }
}
