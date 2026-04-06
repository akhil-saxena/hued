package app.hued.ui.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import app.hued.R
import app.hued.data.model.TimePeriod
import app.hued.ui.main.PeriodPaletteUi
import java.io.File
import java.io.FileOutputStream

/**
 * Share card renderer that matches the in-app Compose typography exactly.
 *
 * Scale mapping: app is ~360dp wide, card is 1080px → 3× scale.
 * Every sp/dp value from Type.kt is multiplied by 3 to get px.
 *
 * App typography reference (Type.kt):
 *   labelSmall  → Light  10sp  ls 0.3sp   → 30px  ls 0.03em
 *   displaySmall → Light  28sp  ls -0.5sp  → 84px  ls -0.018em
 *   bodySmall   → Light  10sp  ls 1.0sp   → 30px  ls 0.1em
 *   headlineSmall → Bold  16sp  ls 6.0sp   → 48px  ls 0.375em
 *   bodyMedium  → Normal 12sp  ls 0.2sp   → 36px  ls 0.017em
 */
object ShareCardRenderer {

    private const val W = 1080
    private const val H_STORY = 1920 // 9:16

    // 3× scale from dp/sp to px
    private const val S = 3f

    // App layout values scaled to card pixels
    private const val MARGIN = 24f * S          // 72px — matches app's 24dp horizontal padding
    private const val STRIP_H = 220f * S         // 660px — >1/3 of 1920px card
    private const val STRIP_H_YEAR = 250f * S   // 750px — year gets even taller
    private const val STRIP_RADIUS = 4f * S     // 12px — matches app's 4dp corner radius

    // Typography — exact 3× of Type.kt values
    private const val LABEL_SIZE = 10f * S      // 30px — labelSmall
    private const val LABEL_LS = 0.03f          // 0.3sp / 10sp in em

    private const val DISPLAY_SIZE = 28f * S    // 84px — displaySmall
    private const val DISPLAY_LS = -0.018f      // -0.5sp / 28sp in em

    private const val BODY_SIZE = 10f * S       // 30px — bodySmall
    private const val BODY_LS = 0.1f            // 1.0sp / 10sp in em

    private const val WORDMARK_SIZE = 16f * S   // 48px — headlineSmall
    private const val WORDMARK_LS = 0.375f      // 6.0sp / 16sp in em

    private const val YEAR_HERO_SIZE = 42f * S  // 126px — larger than displaySmall for year hero
    private const val YEAR_HERO_LS = -0.012f

    private const val STATS_SIZE = 12f * S      // 36px — bodyMedium
    private const val STATS_LS = 0.017f         // 0.2sp / 12sp in em

    // Colors — from Color.kt
    private val TEXT = android.graphics.Color.rgb(0x2A, 0x28, 0x26)       // HuedTextPrimary
    private val MUTED = android.graphics.Color.rgb(0x50, 0x4E, 0x4C)      // HuedTextMutedResting
    private val MUTED_LIGHT = android.graphics.Color.argb(0xBB, 0x50, 0x4E, 0x4C)

    fun renderAndShare(
        context: Context,
        palette: PeriodPaletteUi,
        period: TimePeriod,
        isCurrent: Boolean,
    ) {
        val bitmap = renderStory(context, palette, period, isCurrent)
        shareBitmap(context, bitmap)
        bitmap.recycle()
    }

    fun renderBitmap(
        context: Context,
        palette: PeriodPaletteUi,
        period: TimePeriod,
        isCurrent: Boolean,
    ): Bitmap = renderStory(context, palette, period, isCurrent)

    private fun renderStory(
        context: Context,
        palette: PeriodPaletteUi,
        period: TimePeriod,
        isCurrent: Boolean,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(W, H_STORY, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(deriveBgColor(palette))

        val fonts = loadFonts(context)

        when (period) {
            TimePeriod.WEEK, TimePeriod.MONTH -> drawWeekMonthStory(canvas, palette, period, isCurrent, fonts)
            TimePeriod.YEAR -> drawYearStory(canvas, palette, fonts)
        }

        return bitmap
    }

    // ── A1: Week/Month Story ──
    // Layout: centered vertically
    //   small label (labelSmall Light)
    //   big date (displaySmall Light)
    //   palette strip
    //   poetic (bodySmall Light italic)
    //   HUED wordmark at bottom

    private fun drawWeekMonthStory(
        canvas: Canvas,
        palette: PeriodPaletteUi,
        period: TimePeriod,
        isCurrent: Boolean,
        fonts: Fonts,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = W / 2f
        val stripW = W - MARGIN * 2

        // Derive labels
        val smallLabel: String
        val bigLabel: String
        if (period == TimePeriod.MONTH) {
            // Month card: year small, month name big (capitalized)
            val parts = palette.periodLabel.split(" ", limit = 2)
            if (parts.size == 2) {
                bigLabel = parts[0].replaceFirstChar { it.uppercase() }
                smallLabel = parts[1]
            } else {
                bigLabel = palette.periodLabel.replaceFirstChar { it.uppercase() }
                smallLabel = ""
            }
        } else {
            smallLabel = if (isCurrent) "This Week" else ""
            bigLabel = palette.periodLabel.split(" ").joinToString(" ") {
                it.replaceFirstChar { c -> c.uppercase() }
            }
        }

        // Calculate total content height to center it
        val gap1 = 6f * S    // between label and date (matches app's 6dp spacer)
        val gap2 = 12f * S   // between date and strip (matches app's ~12dp)
        val gap3 = 12f * S   // between strip and poetic (matches app's 12dp)

        val contentH = LABEL_SIZE + gap1 + DISPLAY_SIZE + gap2 + STRIP_H + gap3 + BODY_SIZE
        val startY = (H_STORY - contentH) / 2f

        // Small label — labelSmall: Light 10sp, ls 0.3sp
        paint.typeface = fonts.light
        paint.textSize = LABEL_SIZE
        paint.color = MUTED
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = LABEL_LS
        var y = startY + LABEL_SIZE
        canvas.drawText(smallLabel, cx, y, paint)

        // Big date — displaySmall: Light 28sp, ls -0.5sp
        paint.typeface = fonts.light
        paint.textSize = DISPLAY_SIZE
        paint.color = TEXT
        paint.letterSpacing = DISPLAY_LS
        y += gap1 + DISPLAY_SIZE
        canvas.drawText(bigLabel, cx, y, paint)

        // Strip
        val stripY = y + gap2
        drawStrip(canvas, palette.colors, MARGIN, stripY, stripW, STRIP_H, STRIP_RADIUS)

        // Poetic — bodySmall: Light 10sp italic, ls 1.0sp
        paint.typeface = fonts.lightItalic
        paint.textSize = BODY_SIZE
        paint.color = MUTED
        paint.letterSpacing = BODY_LS
        y = stripY + STRIP_H + gap3 + BODY_SIZE
        canvas.drawText("\u201c${palette.poeticDescription}\u201d", cx, y, paint)

        // Top color swatch + name
        y += gap3
        y = drawTopColorSwatches(canvas, palette, 1, cx, y, fonts)

        drawWordmark(canvas, fonts)
    }

    // ── D1: Year Story ──
    // Layout: centered vertically
    //   large year number
    //   tall palette strip
    //   poetic
    //   stats (image count)
    //   HUED wordmark at bottom

    private fun drawYearStory(
        canvas: Canvas,
        palette: PeriodPaletteUi,
        fonts: Fonts,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = W / 2f
        val stripW = W - MARGIN * 2

        val gap1 = 24f * S   // between year and strip
        val gap2 = 12f * S   // between strip and poetic
        val gap3 = 8f * S    // between poetic and stats

        val contentH = YEAR_HERO_SIZE + gap1 + STRIP_H_YEAR + gap2 + BODY_SIZE + gap3 + STATS_SIZE
        val startY = (H_STORY - contentH) / 2f

        // Year number — large Light
        paint.typeface = fonts.light
        paint.textSize = YEAR_HERO_SIZE
        paint.color = TEXT
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = YEAR_HERO_LS
        var y = startY + YEAR_HERO_SIZE
        canvas.drawText(palette.periodLabel, cx, y, paint)

        // Strip (taller)
        val stripY = y + gap1
        drawStrip(canvas, palette.colors, MARGIN, stripY, stripW, STRIP_H_YEAR, STRIP_RADIUS)

        // Poetic — bodySmall italic
        paint.typeface = fonts.lightItalic
        paint.textSize = BODY_SIZE
        paint.color = MUTED
        paint.letterSpacing = BODY_LS
        y = stripY + STRIP_H_YEAR + gap2 + BODY_SIZE
        canvas.drawText("\u201c${palette.poeticDescription}\u201d", cx, y, paint)

        // Top color swatch + name
        y += gap3
        y = drawTopColorSwatches(canvas, palette, 1, cx, y, fonts)

        // Stats — bodyMedium
        if (palette.photoCount > 0) {
            paint.typeface = fonts.regular
            paint.textSize = STATS_SIZE
            paint.color = MUTED_LIGHT
            paint.letterSpacing = STATS_LS
            canvas.drawText("${palette.photoCount} images", cx, y + gap3 + STATS_SIZE, paint)
        }

        drawWordmark(canvas, fonts)
    }

    /**
     * Draws top N colors as swatch rectangles + name text, centered horizontally.
     * Returns the Y position after drawing.
     */
    private fun drawTopColorSwatches(
        canvas: Canvas,
        palette: PeriodPaletteUi,
        n: Int,
        cx: Float,
        startY: Float,
        fonts: Fonts,
    ): Float {
        val names = palette.colorNames
        val colors = palette.colors
        val weights = palette.colorWeights

        val top = if (weights.size == names.size) {
            names.zip(colors).zip(weights)
                .map { (nc, w) -> Triple(nc.first, nc.second, w) }
                .sortedByDescending { it.third }
                .take(n)
        } else {
            names.zip(colors).take(n).map { (name, color) -> Triple(name, color, 1f) }
        }

        if (top.isEmpty()) return startY

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val swatchW = 28f * S   // 84px
        val swatchH = 14f * S   // 42px
        val swatchRadius = 2f * S
        val gap = 10f * S       // between swatch and text
        val itemGap = 24f * S   // between items

        // Measure total width to center
        paint.typeface = fonts.light
        paint.textSize = LABEL_SIZE
        paint.letterSpacing = LABEL_LS
        val itemWidths = top.map { (name, _, _) ->
            swatchW + gap + paint.measureText(name.lowercase())
        }
        val totalWidth = itemWidths.sum() + itemGap * (top.size - 1)
        var x = cx - totalWidth / 2f
        val y = startY + swatchH // baseline for text aligns with swatch bottom

        top.forEachIndexed { i, (name, color, _) ->
            // Draw swatch
            paint.color = android.graphics.Color.rgb(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
            )
            val rect = RectF(x, startY, x + swatchW, startY + swatchH)
            canvas.drawRoundRect(rect, swatchRadius, swatchRadius, paint)

            // Draw name
            paint.typeface = fonts.light
            paint.textSize = LABEL_SIZE
            paint.color = MUTED_LIGHT
            paint.letterSpacing = LABEL_LS
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(name.lowercase(), x + swatchW + gap, y - 4f * S, paint)

            x += itemWidths[i] + itemGap
        }

        // Reset alignment
        paint.textAlign = Paint.Align.CENTER

        return startY + swatchH
    }

    // ── Drawing Helpers ──

    private fun drawStrip(
        canvas: Canvas,
        colors: List<androidx.compose.ui.graphics.Color>,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        cornerRadius: Float,
    ) {
        if (colors.isEmpty()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bandWidth = width / colors.size

        canvas.save()
        val path = android.graphics.Path()
        path.addRoundRect(
            RectF(x, y, x + width, y + height),
            cornerRadius, cornerRadius,
            android.graphics.Path.Direction.CW,
        )
        canvas.clipPath(path)

        colors.forEachIndexed { index, color ->
            paint.color = android.graphics.Color.rgb(
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt(),
            )
            canvas.drawRect(
                x + index * bandWidth, y,
                x + (index + 1) * bandWidth, y + height,
                paint,
            )
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
        canvas.drawText("HUED", W / 2f, H_STORY - 32f * S, paint)
    }

    // ── Background ──

    private fun deriveBgColor(palette: PeriodPaletteUi): Int {
        val dominant = palette.colors.firstOrNull()
            ?: return android.graphics.Color.rgb(0xF8, 0xF7, 0xF5)
        val warmth = dominant.red - dominant.blue
        val r = (0xF8 + (warmth * 0.08f * 255).toInt()).coerceIn(0xE8, 0xFF)
        val g = 0xF7
        val b = (0xF5 - (warmth * 0.08f * 255).toInt()).coerceIn(0xE8, 0xFF)
        return android.graphics.Color.rgb(r, g, b)
    }

    // ── Fonts ──

    private data class Fonts(
        val light: Typeface,
        val lightItalic: Typeface,
        val regular: Typeface,
        val bold: Typeface,
    )

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

    // ── Share Intent ──

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
