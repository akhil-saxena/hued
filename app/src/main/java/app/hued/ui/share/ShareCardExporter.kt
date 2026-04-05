package app.hued.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Environment
import androidx.compose.ui.graphics.Color
import androidx.core.content.res.ResourcesCompat
import app.hued.R
import java.io.File
import java.io.FileOutputStream

/**
 * Exports all share card layout variants as PNGs for review.
 * Uses the same rendering logic as ShareCardRenderer with exact typography matching.
 */
object ShareCardExporter {

    private const val W = 1080
    private const val H = 1920

    private const val S = 3f

    private const val MARGIN = 24f * S
    private const val STRIP_H = 220f * S       // 660px — >1/3 of 1920px card
    private const val STRIP_H_YEAR = 250f * S  // 750px — year gets even taller
    private const val STRIP_RADIUS = 4f * S

    // Typography — exact 3× of Type.kt
    private const val LABEL_SIZE = 10f * S
    private const val LABEL_LS = 0.03f

    private const val DISPLAY_SIZE = 28f * S
    private const val DISPLAY_LS = -0.018f

    private const val BODY_SIZE = 10f * S
    private const val BODY_LS = 0.1f

    private const val WORDMARK_SIZE = 16f * S
    private const val WORDMARK_LS = 0.375f

    private const val YEAR_HERO_SIZE = 42f * S
    private const val YEAR_HERO_LS = -0.012f

    private const val STATS_SIZE = 12f * S
    private const val STATS_LS = 0.017f

    private val TEXT = android.graphics.Color.rgb(0x2A, 0x28, 0x26)
    private val MUTED = android.graphics.Color.rgb(0x8A, 0x88, 0x85)
    private val MUTED_LIGHT = android.graphics.Color.argb(0x99, 0x8A, 0x88, 0x85)
    private val BG = android.graphics.Color.rgb(0xF8, 0xF7, 0xF5)

    // Sample data
    private val colors = listOf(
        Color(0xFFCB6D51),
        Color(0xFF3B6B8A),
        Color(0xFF4A7C59),
        Color(0xFFF5E6D3),
        Color(0xFFC49540),
    )
    private const val poetic = "dust and gold and quiet blue"

    fun exportAll(context: Context): Int {
        val fonts = loadFonts(context)
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "hued_share_exports",
        )
        dir.mkdirs()

        val cards = listOf(
            "01_this_week" to renderWeekMonth(fonts, smallLabel = "This Week", bigLabel = "Mar 31 \u2013 Apr 6"),
            "02_earlier_week" to renderWeekMonth(fonts, smallLabel = "", bigLabel = "Mar 24 \u2013 30"),
            "03_this_month" to renderWeekMonth(fonts, smallLabel = "2026", bigLabel = "April"),
            "04_earlier_month" to renderWeekMonth(fonts, smallLabel = "2026", bigLabel = "February"),
            "05_this_year" to renderYear(fonts, year = "2026", photoCount = 1247),
            "06_earlier_year" to renderYear(fonts, year = "2024", photoCount = 2891),
        )

        cards.forEach { (name, bitmap) ->
            val file = File(dir, "$name.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
            bitmap.recycle()
        }

        return cards.size
    }

    // ── A1: Week/Month ──

    private fun renderWeekMonth(fonts: Fonts, smallLabel: String, bigLabel: String): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BG)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = W / 2f
        val stripW = W - MARGIN * 2

        val gap1 = 6f * S
        val gap2 = 12f * S
        val gap3 = 12f * S

        val contentH = LABEL_SIZE + gap1 + DISPLAY_SIZE + gap2 + STRIP_H + gap3 + BODY_SIZE
        val startY = (H - contentH) / 2f

        // Small label
        paint.typeface = fonts.light
        paint.textSize = LABEL_SIZE
        paint.color = MUTED
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = LABEL_LS
        var y = startY + LABEL_SIZE
        canvas.drawText(smallLabel, cx, y, paint)

        // Big label
        paint.typeface = fonts.light
        paint.textSize = DISPLAY_SIZE
        paint.color = TEXT
        paint.letterSpacing = DISPLAY_LS
        y += gap1 + DISPLAY_SIZE
        canvas.drawText(bigLabel, cx, y, paint)

        // Strip
        val stripY = y + gap2
        drawStrip(canvas, colors, MARGIN, stripY, stripW, STRIP_H, STRIP_RADIUS)

        // Poetic
        paint.typeface = fonts.lightItalic
        paint.textSize = BODY_SIZE
        paint.color = MUTED
        paint.letterSpacing = BODY_LS
        y = stripY + STRIP_H + gap3 + BODY_SIZE
        canvas.drawText("\u201c$poetic\u201d", cx, y, paint)

        // Wordmark
        drawWordmark(canvas, fonts)

        return bitmap
    }

    // ── D1: Year ──

    private fun renderYear(fonts: Fonts, year: String, photoCount: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BG)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = W / 2f
        val stripW = W - MARGIN * 2

        val gap1 = 24f * S
        val gap2 = 12f * S
        val gap3 = 8f * S

        val contentH = YEAR_HERO_SIZE + gap1 + STRIP_H_YEAR + gap2 + BODY_SIZE + gap3 + STATS_SIZE
        val startY = (H - contentH) / 2f

        // Year
        paint.typeface = fonts.light
        paint.textSize = YEAR_HERO_SIZE
        paint.color = TEXT
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = YEAR_HERO_LS
        var y = startY + YEAR_HERO_SIZE
        canvas.drawText(year, cx, y, paint)

        // Strip
        val stripY = y + gap1
        drawStrip(canvas, colors, MARGIN, stripY, stripW, STRIP_H_YEAR, STRIP_RADIUS)

        // Poetic
        paint.typeface = fonts.lightItalic
        paint.textSize = BODY_SIZE
        paint.color = MUTED
        paint.letterSpacing = BODY_LS
        y = stripY + STRIP_H_YEAR + gap2 + BODY_SIZE
        canvas.drawText("\u201c$poetic\u201d", cx, y, paint)

        // Stats
        paint.typeface = fonts.regular
        paint.textSize = STATS_SIZE
        paint.color = MUTED_LIGHT
        paint.letterSpacing = STATS_LS
        canvas.drawText("$photoCount images", cx, y + gap3 + STATS_SIZE, paint)

        // Wordmark
        drawWordmark(canvas, fonts)

        return bitmap
    }

    // ── Helpers ──

    private fun drawStrip(
        canvas: Canvas,
        colors: List<Color>,
        x: Float, y: Float, width: Float, height: Float, cornerRadius: Float,
    ) {
        if (colors.isEmpty()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bandW = width / colors.size

        canvas.save()
        val path = android.graphics.Path()
        path.addRoundRect(RectF(x, y, x + width, y + height), cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
        canvas.clipPath(path)

        colors.forEachIndexed { i, color ->
            paint.color = android.graphics.Color.rgb(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
            )
            canvas.drawRect(x + i * bandW, y, x + (i + 1) * bandW, y + height, paint)
        }
        canvas.restore()
    }

    private fun drawWordmark(canvas: Canvas, fonts: Fonts) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = fonts.bold
            textSize = WORDMARK_SIZE
            color = TEXT
            textAlign = Paint.Align.CENTER
            letterSpacing = WORDMARK_LS
        }
        canvas.drawText("HUED", W / 2f, H - 32f * S, paint)
    }

    private data class Fonts(val light: Typeface, val lightItalic: Typeface, val regular: Typeface, val bold: Typeface)

    private fun loadFonts(context: Context): Fonts {
        val light = ResourcesCompat.getFont(context, R.font.outfit_light)
            ?: Typeface.create("sans-serif-light", Typeface.NORMAL)
        return Fonts(
            light = light,
            lightItalic = Typeface.create(light, Typeface.ITALIC),
            regular = ResourcesCompat.getFont(context, R.font.outfit_regular)
                ?: Typeface.create("sans-serif", Typeface.NORMAL),
            bold = ResourcesCompat.getFont(context, R.font.outfit_bold)
                ?: Typeface.create("sans-serif", Typeface.BOLD),
        )
    }
}
