package app.hued.domain

import android.graphics.Color
import app.hued.data.local.entity.DelightStateEntity
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.model.DelightMoment
import app.hued.data.repository.DelightRepository
import app.hued.processing.ColorNamer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject

class DetectDelightMomentUseCase @Inject constructor(
    private val delightRepository: DelightRepository,
    private val colorNamer: ColorNamer,
) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun detect(palette: PeriodPaletteEntity): DelightMoment? {
        val periodKey = "${palette.periodType}_${palette.startDate}"

        // Check if already shown
        val existing = delightRepository.getState("MONOCHROME", periodKey)
            ?: delightRepository.getState("HARMONY", periodKey)
            ?: delightRepository.getState("BIRTHDAY", periodKey)
            ?: delightRepository.getState("NEW_YEAR", periodKey)
        if (existing?.shown == true) return null

        val hexColors = json.decodeFromString<List<String>>(palette.colors)

        // Monochrome detection — single hue family dominates >85%
        val monochrome = detectMonochrome(hexColors, periodKey)
        if (monochrome != null) return monochrome

        // Harmony detection — warm/cool balance within 10%
        val harmony = detectHarmony(hexColors, periodKey)
        if (harmony != null) return harmony

        // Birthday month
        val birthday = detectBirthday(palette, periodKey)
        if (birthday != null) return birthday

        // New Year
        val newYear = detectNewYear(palette, periodKey)
        if (newYear != null) return newYear

        return null
    }

    private suspend fun detectMonochrome(hexColors: List<String>, periodKey: String): DelightMoment.Monochrome? {
        if (hexColors.isEmpty()) return null

        val hues = hexColors.mapNotNull { hex ->
            try {
                val color = Color.parseColor(hex)
                val hsv = FloatArray(3)
                Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv)
                hsv[0] // hue 0-360
            } catch (_: Exception) { null }
        }

        if (hues.isEmpty()) return null

        // Check if all hues are within 30 degrees
        val avgHue = hues.average().toFloat()
        val allClose = hues.all { hue ->
            val diff = kotlin.math.abs(hue - avgHue)
            diff < 30f || diff > 330f
        }

        if (!allClose) return null

        val dominantName = colorNamer.getName(hexColors.first())
        val hueName = when {
            avgHue < 30 || avgHue > 330 -> "red"
            avgHue < 90 -> "orange"
            avgHue < 150 -> "green"
            avgHue < 210 -> "blue"
            avgHue < 270 -> "purple"
            else -> "pink"
        }

        val moment = DelightMoment.Monochrome(
            text = "A month in $hueName. Nothing but $hueName.",
            periodKey = periodKey,
            hueName = hueName,
        )

        delightRepository.saveState(
            DelightStateEntity(type = "MONOCHROME", periodKey = periodKey, shown = false)
        )

        return moment
    }

    private suspend fun detectHarmony(hexColors: List<String>, periodKey: String): DelightMoment.Harmony? {
        if (hexColors.size < 3) return null

        var warmCount = 0
        var coolCount = 0

        hexColors.forEach { hex ->
            try {
                val color = Color.parseColor(hex)
                if (Color.red(color) > Color.blue(color)) warmCount++ else coolCount++
            } catch (_: Exception) {}
        }

        val total = warmCount + coolCount
        if (total == 0) return null

        val balance = kotlin.math.abs(warmCount - coolCount).toFloat() / total
        if (balance > 0.1f) return null

        val moment = DelightMoment.Harmony(
            text = "Perfectly balanced. Warm and cool in equal measure.",
            periodKey = periodKey,
        )

        delightRepository.saveState(
            DelightStateEntity(type = "HARMONY", periodKey = periodKey, shown = false)
        )

        return moment
    }

    private suspend fun detectBirthday(palette: PeriodPaletteEntity, periodKey: String): DelightMoment.Birthday? {
        // Check if this month matches device's current month (simplified birthday detection)
        val paletteDate = LocalDate.ofEpochDay(palette.startDate)
        val now = LocalDate.now()
        if (paletteDate.month != now.month || paletteDate.year != now.year) return null

        // Only trigger in the user's birthday month — would need device date settings
        // For now, skip unless we have a way to detect birthday
        return null
    }

    private suspend fun detectNewYear(palette: PeriodPaletteEntity, periodKey: String): DelightMoment.NewYear? {
        val now = LocalDate.now()
        if (now.monthValue !in 12..12 || now.dayOfMonth < 28) return null
        if (palette.periodType != "YEAR") return null

        val moment = DelightMoment.NewYear(
            text = "Your ${now.year} in Color is ready.",
            periodKey = periodKey,
            year = now.year,
        )

        delightRepository.saveState(
            DelightStateEntity(type = "NEW_YEAR", periodKey = periodKey, shown = false)
        )

        return moment
    }
}
