package app.hued.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
            val palette = Palette.from(bitmap).maximumColorCount(maxColors * 2).generate()

            // Filter out noise swatches (< 3% of total pixels)
            val totalPixels = palette.swatches.sumOf { it.population }
            val minPopulation = (totalPixels * 0.03).toInt()

            val colors = palette.swatches
                .filter { it.population >= minPopulation }
                .sortedByDescending { it.population }
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
