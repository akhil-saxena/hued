package app.hued.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import javax.inject.Inject

data class ExtractedColors(
    val hexColors: List<String>,
)

class PaletteExtractor @Inject constructor(
    private val context: Context,
) {

    private val targetSize = 300 // downsampled for fast extraction, 300px retains secondary colors better

    fun extract(imageUri: Uri, maxColors: Int = 5): ExtractedColors? {
        return try {
            val bitmap = loadDownsampled(imageUri) ?: return null
            val palette = Palette.from(bitmap).maximumColorCount(maxColors * 4).generate()

            // Filter out noise swatches (< 2% of total pixels)
            val totalPixels = palette.swatches.sumOf { it.population }
            val minPopulation = (totalPixels * 0.02).toInt()

            val viableSwatches = palette.swatches
                .filter { it.population >= minPopulation }
                .filter { swatch ->
                    val hsl = FloatArray(3)
                    ColorUtils.colorToHSL(swatch.rgb, hsl)
                    val saturation = hsl[1]
                    val lightness = hsl[2]
                    // Reject: near-black (L<0.10), near-white (L>0.92)
                    if (lightness < 0.10f || lightness > 0.92f) return@filter false
                    // Reject: greys — low saturation across all lightness ranges
                    if (saturation < 0.15f) return@filter false
                    true
                }
                .sortedByDescending { swatch ->
                    val hsl = FloatArray(3)
                    ColorUtils.colorToHSL(swatch.rgb, hsl)
                    // Score: population × saturation^2 — strongly favors colorful swatches
                    val satBoost = 1.0 + hsl[1].toDouble() * hsl[1].toDouble() * 4.0
                    swatch.population * satBoost
                }

            // If aggressive filtering killed everything, fall back to less strict
            val candidates = viableSwatches.ifEmpty {
                palette.swatches
                    .filter { it.population >= minPopulation }
                    .sortedByDescending { it.population }
            }

            val colors = candidates
                .take(maxColors)
                .map { swatch -> String.format("#%06X", 0xFFFFFF and swatch.rgb) }

            if (colors.isEmpty()) null else ExtractedColors(colors)
        } catch (e: Exception) {
            android.util.Log.w("PaletteExtractor", "Failed to extract colors from $imageUri", e)
            null
        }
    }

    private fun loadDownsampled(uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
