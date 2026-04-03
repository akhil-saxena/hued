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

    private val targetSize = 200 // downsampled for fast extraction

    fun extract(imageUri: Uri, maxColors: Int = 5): ExtractedColors? {
        return try {
            val bitmap = loadDownsampled(imageUri) ?: return null
            val palette = Palette.from(bitmap).maximumColorCount(maxColors).generate()
            val colors = palette.swatches
                .sortedByDescending { it.population }
                .take(maxColors)
                .map { swatch -> String.format("#%06X", 0xFFFFFF and swatch.rgb) }

            if (colors.isEmpty()) null else ExtractedColors(colors)
        } catch (e: Exception) {
            null // silently skip corrupt images
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
