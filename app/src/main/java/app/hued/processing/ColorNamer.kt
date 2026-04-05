package app.hued.processing

import android.content.Context
import android.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.sqrt

@Serializable
data class ColorNameEntry(
    val name: String,
    val hex: String,
)

class ColorNamer @Inject constructor(
    private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val colorNames: List<ColorNameEntry> by lazy { loadColorNames() }
    private val cache = LinkedHashMap<String, String>(128, 0.75f, true)

    fun getName(hexColor: String): String {
        cache[hexColor]?.let { return it }
        val target = parseHex(hexColor) ?: return hexColor
        val name = colorNames.minByOrNull { entry ->
            val entryRgb = parseHex(entry.hex) ?: return@minByOrNull Double.MAX_VALUE
            colorDistance(target, entryRgb)
        }?.name ?: hexColor
        cache[hexColor] = name
        return name
    }

    private fun loadColorNames(): List<ColorNameEntry> {
        return try {
            val jsonString = context.resources.openRawResource(
                context.resources.getIdentifier("color_names", "raw", context.packageName)
            ).bufferedReader().use { it.readText() }
            json.decodeFromString<List<ColorNameEntry>>(jsonString)
        } catch (e: Exception) {
            android.util.Log.e("ColorNamer", "Failed to load color names database", e)
            emptyList()
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

    private fun colorDistance(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Double {
        val dr = (a.first - b.first).toDouble()
        val dg = (a.second - b.second).toDouble()
        val db = (a.third - b.third).toDouble()
        return sqrt(dr * dr + dg * dg + db * db)
    }
}
