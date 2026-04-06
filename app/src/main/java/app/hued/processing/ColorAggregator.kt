package app.hued.processing

import android.graphics.Color
import android.util.Log
import androidx.core.graphics.ColorUtils
import app.hued.data.local.entity.PaletteResultEntity
import app.hued.data.local.entity.PeriodPaletteEntity
import app.hued.data.model.TimePeriod
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.sqrt

class ColorAggregator @Inject constructor(
    private val colorNamer: ColorNamer,
    private val poeticDescriptionMatcher: PoeticDescriptionMatcher,
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val MERGE_THRESHOLD = 15.0
    }

    fun aggregate(
        results: List<PaletteResultEntity>,
        periodType: TimePeriod,
        startDate: LocalDate,
        endDate: LocalDate,
        maxColors: Int = 5,
    ): PeriodPaletteEntity {
        // 1. Flatten colors with frequency AND first-seen timestamp per hex
        val hexCounts = mutableMapOf<String, Int>()
        val hexFirstSeen = mutableMapOf<String, Long>()

        // Results are sorted by timestamp DESC, so process in reverse for first-seen
        results.sortedBy { it.timestamp }.forEach { result ->
            try {
                val colors = json.decodeFromString<List<String>>(result.colors)
                colors.forEach { hex ->
                    hexCounts[hex] = (hexCounts[hex] ?: 0) + 1
                    if (hex !in hexFirstSeen) {
                        hexFirstSeen[hex] = result.timestamp
                    }
                }
            } catch (e: Exception) {
                Log.w("ColorAggregator", "Failed to decode colors for result ${result.imageUri}", e)
            }
        }

        // 2. Cluster perceptually similar colors in LAB space
        val clusters = clusterByPerceptualDistance(hexCounts, hexFirstSeen)

        // 3. Separate colorful clusters from grey/neutral ones
        val (colorful, neutral) = clusters.partition { cluster ->
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(Color.parseColor(cluster.representativeHex), hsl)
            hsl[1] >= 0.15f && hsl[2] in 0.10f..0.90f
        }

        // Prefer colorful clusters, fill remaining with neutrals if needed
        val ranked = colorful
            .sortedByDescending { cluster ->
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(Color.parseColor(cluster.representativeHex), hsl)
                val satBoost = 1.0 + hsl[1].toDouble() * hsl[1].toDouble() * 4.0
                cluster.totalCount * satBoost
            }
        val topByWeight = if (ranked.size >= maxColors) {
            ranked.take(maxColors)
        } else {
            ranked + neutral.sortedByDescending { it.totalCount }.take(maxColors - ranked.size)
        }

        // 4. Sort the strip CHRONOLOGICALLY (by earliest appearance in the period)
        //    This makes the strip read left-to-right as a time journey:
        //    Monday's blue | Wednesday's red | Friday's green
        val chronological = topByWeight.sortedBy { it.firstSeenTimestamp }

        val dominantColors = chronological.map { it.representativeHex }
        val totalCount = chronological.sumOf { it.totalCount }.toFloat().coerceAtLeast(1f)
        val weights = chronological.map { it.totalCount / totalCount }

        // Dominant = highest weight (may not be first in the chronological strip)
        val dominant = topByWeight.first().representativeHex
        val poeticDescription = poeticDescriptionMatcher.match(dominantColors, startDate.toEpochDay())

        return PeriodPaletteEntity(
            periodType = periodType.name,
            startDate = startDate.toEpochDay(),
            endDate = endDate.toEpochDay(),
            colors = json.encodeToString(ListSerializer(String.serializer()), dominantColors),
            photoCount = results.size,
            dominantColor = dominant,
            poeticDescription = poeticDescription,
            colorWeights = json.encodeToString(ListSerializer(Float.serializer()), weights),
        )
    }

    private data class ColorCluster(
        val representativeHex: String,
        val lab: DoubleArray,
        var totalCount: Int,
        var firstSeenTimestamp: Long,
    )

    private fun clusterByPerceptualDistance(
        hexCounts: Map<String, Int>,
        hexFirstSeen: Map<String, Long>,
    ): List<ColorCluster> {
        val sorted = hexCounts.entries.sortedByDescending { it.value }
        val clusters = mutableListOf<ColorCluster>()

        for ((hex, count) in sorted) {
            val lab = hexToLab(hex) ?: continue
            val firstSeen = hexFirstSeen[hex] ?: Long.MAX_VALUE

            val nearest = clusters.minByOrNull { deltaE(lab, it.lab) }
            val distance = nearest?.let { deltaE(lab, it.lab) } ?: Double.MAX_VALUE

            if (distance < MERGE_THRESHOLD && nearest != null) {
                nearest.totalCount += count
                // Keep the earliest first-seen across merged colors
                if (firstSeen < nearest.firstSeenTimestamp) {
                    nearest.firstSeenTimestamp = firstSeen
                }
            } else {
                clusters.add(ColorCluster(hex, lab, count, firstSeen))
            }
        }

        return clusters
    }

    private fun hexToLab(hex: String): DoubleArray? {
        return try {
            val color = Color.parseColor(hex)
            val lab = DoubleArray(3)
            ColorUtils.colorToLAB(color, lab)
            lab
        } catch (_: Exception) {
            null
        }
    }

    private fun deltaE(lab1: DoubleArray, lab2: DoubleArray): Double {
        val dL = lab1[0] - lab2[0]
        val dA = lab1[1] - lab2[1]
        val dB = lab1[2] - lab2[2]
        return sqrt(dL * dL + dA * dA + dB * dB)
    }
}
