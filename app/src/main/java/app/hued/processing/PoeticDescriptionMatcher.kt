package app.hued.processing

import android.content.Context
import android.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class PoeticTemplate(
    val warmth: String, // warm, cool, neutral
    val saturation: String, // vibrant, muted, mixed
    val descriptions: List<String>,
)

class PoeticDescriptionMatcher @Inject constructor(
    private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val templates: List<PoeticTemplate> by lazy { loadTemplates() }

    fun match(hexColors: List<String>): String {
        val warmth = analyzeWarmth(hexColors)
        val saturation = analyzeSaturation(hexColors)

        val matching = templates.filter { t ->
            t.warmth == warmth && t.saturation == saturation
        }.flatMap { it.descriptions }

        if (matching.isEmpty()) {
            val fallback = templates.flatMap { it.descriptions }
            return fallback.randomOrNull() ?: ""
        }

        return matching.random()
    }

    private fun analyzeWarmth(hexColors: List<String>): String {
        val warmCount = hexColors.count { hex ->
            val rgb = parseHex(hex) ?: return@count false
            rgb.first > rgb.third // red > blue = warm
        }
        val ratio = warmCount.toFloat() / hexColors.size.coerceAtLeast(1)
        return when {
            ratio > 0.6f -> "warm"
            ratio < 0.4f -> "cool"
            else -> "neutral"
        }
    }

    private fun analyzeSaturation(hexColors: List<String>): String {
        val avgSaturation = hexColors.mapNotNull { hex ->
            val rgb = parseHex(hex) ?: return@mapNotNull null
            val hsv = FloatArray(3)
            Color.RGBToHSV(rgb.first, rgb.second, rgb.third, hsv)
            hsv[1]
        }.average().toFloat()

        return when {
            avgSaturation > 0.5f -> "vibrant"
            avgSaturation < 0.25f -> "muted"
            else -> "mixed"
        }
    }

    private fun parseHex(hex: String): Triple<Int, Int, Int>? {
        return try {
            val color = Color.parseColor(hex)
            Triple(Color.red(color), Color.green(color), Color.blue(color))
        } catch (e: Exception) {
            null
        }
    }

    private fun loadTemplates(): List<PoeticTemplate> {
        return try {
            val jsonString = context.assets.open("poetic_templates.json")
                .bufferedReader().use { it.readText() }
            json.decodeFromString<List<PoeticTemplate>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
