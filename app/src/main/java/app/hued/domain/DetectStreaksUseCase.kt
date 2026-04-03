package app.hued.domain

import android.graphics.Color
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.local.entity.StreakDataEntity
import app.hued.data.repository.PaletteRepository
import app.hued.data.repository.StreakRepository
import app.hued.data.model.TimePeriod
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

class DetectStreaksUseCase @Inject constructor(
    private val paletteRepository: PaletteRepository,
    private val streakRepository: StreakRepository,
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun detectStreaks() {
        val palettes = paletteRepository.getAllPalettes(TimePeriod.WEEK).first()
        if (palettes.size < 2) return

        val sorted = palettes.sortedBy { it.startDate }
        var streakStart = sorted.first()
        var streakFamily = classifyToneFamily(streakStart)
        var streakCount = 1

        for (i in 1 until sorted.size) {
            val current = sorted[i]
            val currentFamily = classifyToneFamily(current)

            if (currentFamily == streakFamily) {
                streakCount++
            } else {
                if (streakCount >= 2) {
                    saveStreak(streakStart, sorted[i - 1], streakFamily, streakCount, isActive = false)
                }
                streakStart = current
                streakFamily = currentFamily
                streakCount = 1
            }
        }

        // Save current active streak
        if (streakCount >= 2) {
            saveStreak(streakStart, sorted.last(), streakFamily, streakCount, isActive = true)
        }
    }

    private fun classifyToneFamily(palette: PeriodPaletteEntity): String {
        val hexColors = json.decodeFromString<List<String>>(palette.colors)
        var warmCount = 0
        var coolCount = 0

        hexColors.forEach { hex ->
            try {
                val color = Color.parseColor(hex)
                val r = Color.red(color)
                val b = Color.blue(color)
                if (r > b + 30) warmCount++ else if (b > r + 30) coolCount++
            } catch (_: Exception) {}
        }

        return when {
            warmCount > coolCount -> "warm"
            coolCount > warmCount -> "cool"
            else -> "neutral"
        }
    }

    private suspend fun saveStreak(
        start: PeriodPaletteEntity,
        end: PeriodPaletteEntity,
        family: String,
        count: Int,
        isActive: Boolean,
    ) {
        streakRepository.saveStreak(
            StreakDataEntity(
                startDate = start.startDate,
                endDate = end.endDate,
                toneFamily = family,
                dayCount = count * 7, // weeks to days
                isActive = isActive,
            )
        )
    }
}
