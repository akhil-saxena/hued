package app.hued.processing

import android.content.Context
import android.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.absoluteValue

@Serializable
data class PoeticTemplate(
    val warmth: String,     // warm, cool, neutral
    val saturation: String, // vibrant, muted, mixed
    val lightness: String,  // light, mid, dark
    val descriptions: List<String>,
)

class PoeticDescriptionMatcher @Inject constructor(
    private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val templates: List<PoeticTemplate> by lazy { loadTemplates() }

    /**
     * Returns a poetic description for the given colors.
     * Uses [seedEpochDay] for deterministic, collision-resistant selection.
     * Appends the dominant color name as a subtle suffix.
     */
    fun match(hexColors: List<String>, seedEpochDay: Long = 0L): String {
        if (hexColors.isEmpty()) return ""

        val warmth = analyzeWarmth(hexColors)
        val saturation = analyzeSaturation(hexColors)
        val lightness = analyzeLightness(hexColors)

        // Try exact match first, then relax lightness, then all
        val pool = findDescriptions(warmth, saturation, lightness)
            .ifEmpty { findDescriptions(warmth, saturation, null) }
            .ifEmpty { templates.flatMap { it.descriptions } }

        if (pool.isEmpty()) return ""

        // Deterministic hash with good distribution
        val hash = murmurish(seedEpochDay)
        val index = (hash % pool.size).toInt().absoluteValue
        return pool[index]
    }

    private fun findDescriptions(warmth: String, saturation: String, lightness: String?): List<String> {
        return templates.filter { t ->
            t.warmth == warmth && t.saturation == saturation && (lightness == null || t.lightness == lightness)
        }.flatMap { it.descriptions }
    }

    /**
     * HSV hue-based warmth. Low-saturation colors (greys) are excluded from the vote.
     */
    private fun analyzeWarmth(hexColors: List<String>): String {
        var warmVotes = 0f
        var coolVotes = 0f
        var totalWeight = 0f

        hexColors.forEach { hex ->
            val hsv = hexToHsv(hex) ?: return@forEach
            val hue = hsv[0]
            val sat = hsv[1]

            // Low saturation = grey/white/black, skip for warmth
            if (sat < 0.15f) return@forEach

            val weight = sat // more saturated = stronger vote
            totalWeight += weight

            when {
                hue < 60f || hue > 300f -> warmVotes += weight   // reds, oranges, yellows, magentas
                hue in 180f..300f -> coolVotes += weight          // blues, purples
                // 60-180 = greens/cyans, slightly cool-leaning
                else -> coolVotes += weight * 0.3f
            }
        }

        if (totalWeight < 0.01f) return "neutral"
        val warmRatio = warmVotes / totalWeight
        val coolRatio = coolVotes / totalWeight
        return when {
            warmRatio > 0.55f -> "warm"
            coolRatio > 0.55f -> "cool"
            else -> "neutral"
        }
    }

    private fun analyzeSaturation(hexColors: List<String>): String {
        val sats = hexColors.mapNotNull { hexToHsv(it)?.get(1) }
        if (sats.isEmpty()) return "mixed"
        val avg = sats.average().toFloat()
        return when {
            avg > 0.45f -> "vibrant"
            avg < 0.2f -> "muted"
            else -> "mixed"
        }
    }

    private fun analyzeLightness(hexColors: List<String>): String {
        val lightnesses = hexColors.mapNotNull { hex ->
            try {
                val color = Color.parseColor(hex)
                val lab = DoubleArray(3)
                androidx.core.graphics.ColorUtils.colorToLAB(color, lab)
                lab[0] // L* in CIELAB, 0=black, 100=white
            } catch (_: Exception) { null }
        }
        if (lightnesses.isEmpty()) return "mid"
        val avg = lightnesses.average()
        return when {
            avg > 70.0 -> "light"
            avg < 35.0 -> "dark"
            else -> "mid"
        }
    }

    private fun hexToHsv(hex: String): FloatArray? {
        return try {
            val color = Color.parseColor(hex)
            val hsv = FloatArray(3)
            Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv)
            hsv
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Simple but well-distributed hash for epoch day seeds.
     * Much better than `seed * 31` for avoiding adjacent-period collisions.
     */
    private fun murmurish(seed: Long): Long {
        var h = seed
        h = h xor (h ushr 16)
        h *= 0x45d9f3bL
        h = h xor (h ushr 16)
        h *= 0x45d9f3bL
        h = h xor (h ushr 16)
        return h.absoluteValue
    }

    private fun loadTemplates(): List<PoeticTemplate> {
        return try {
            val jsonString = context.assets.open("poetic_templates.json")
                .bufferedReader().use { it.readText() }
            json.decodeFromString<List<PoeticTemplate>>(jsonString)
        } catch (e: Exception) {
            android.util.Log.e("PoeticMatcher", "Failed to load poetic templates", e)
            emptyList()
        }
    }
}
