package app.hued.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import app.hued.ui.main.PeriodPaletteUi
import app.hued.util.toComposeColor
import java.io.File
import java.io.FileOutputStream

object ShareCardRenderer {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT_STORY = 1920 // 9:16
    private const val CARD_HEIGHT_SQUARE = 1080 // 1:1

    fun renderAndShare(
        context: Context,
        palette: PeriodPaletteUi,
        isSquare: Boolean = false,
    ) {
        val bitmap = renderCard(context, palette, isSquare)
        shareBitmap(context, bitmap)
        bitmap.recycle()
    }

    private fun renderCard(
        context: Context,
        palette: PeriodPaletteUi,
        isSquare: Boolean,
    ): Bitmap {
        val height = if (isSquare) CARD_HEIGHT_SQUARE else CARD_HEIGHT_STORY
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Derive canvas color from dominant
        val dominantColor = palette.colors.firstOrNull()
        val bgColor = if (dominantColor != null) {
            val r = (0xF8 + ((dominantColor.red - dominantColor.blue) * 0.08f * 255).toInt()).coerceIn(0xE8, 0xFF)
            val g = 0xF7
            val b = (0xF5 - ((dominantColor.red - dominantColor.blue) * 0.08f * 255).toInt()).coerceIn(0xE8, 0xFF)
            android.graphics.Color.rgb(r, g, b)
        } else {
            android.graphics.Color.rgb(0xF8, 0xF7, 0xF5)
        }

        canvas.drawColor(bgColor)

        val textColor = android.graphics.Color.rgb(0x2A, 0x28, 0x26)
        val mutedColor = android.graphics.Color.rgb(0x8A, 0x88, 0x85)

        // Load Outfit font
        val outfitLight = try {
            Typeface.createFromAsset(context.assets, "../res/font/outfit_light.otf")
        } catch (e: Exception) {
            Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        val outfitBold = try {
            Typeface.createFromAsset(context.assets, "../res/font/outfit_bold.otf")
        } catch (e: Exception) {
            Typeface.create("sans-serif", Typeface.BOLD)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val margin = 80f
        val stripWidth = CARD_WIDTH - (margin * 2)

        // Month label
        paint.typeface = outfitLight
        paint.textSize = 64f
        paint.color = textColor
        val monthY = if (isSquare) 200f else 400f
        canvas.drawText(palette.periodLabel, margin, monthY, paint)

        // Tagline
        paint.textSize = 32f
        paint.color = mutedColor
        canvas.drawText("your life in color", margin, monthY + 50f, paint)

        // Palette strip
        val stripY = monthY + 100f
        val stripHeight = if (isSquare) 200f else 320f
        val bandWidth = stripWidth / palette.colors.size.coerceAtLeast(1)

        palette.colors.forEachIndexed { index, color ->
            paint.color = android.graphics.Color.rgb(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
            )
            canvas.drawRect(
                margin + (index * bandWidth),
                stripY,
                margin + ((index + 1) * bandWidth),
                stripY + stripHeight,
                paint,
            )
        }

        // HUED wordmark at bottom
        paint.typeface = outfitBold
        paint.textSize = 48f
        paint.color = textColor
        paint.letterSpacing = 0.3f
        val wordmark = "HUED"
        val wordmarkWidth = paint.measureText(wordmark)
        val wordmarkY = height - 120f
        canvas.drawText(wordmark, (CARD_WIDTH - wordmarkWidth) / 2f, wordmarkY, paint)

        return bitmap
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap) {
        val cachePath = File(context.cacheDir, "share_cards")
        cachePath.mkdirs()
        val file = File(cachePath, "hued_palette.png")

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
