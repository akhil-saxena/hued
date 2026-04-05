package app.hued.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hued.ui.theme.HuedCanvasResting
import app.hued.ui.theme.HuedTextMutedResting
import app.hued.ui.theme.HuedTextPrimary
import app.hued.ui.theme.OutfitFontFamily

// ── Sample data ──

private val sampleColors = listOf(
    Color(0xFFCB6D51),
    Color(0xFF3B6B8A),
    Color(0xFF4A7C59),
    Color(0xFFF5E6D3),
    Color(0xFFC49540),
)
private val samplePoetic = "dust and gold and quiet blue"

private val ytdPalettes = listOf(
    "jan" to listOf(Color(0xFF8AACBF), Color(0xFFB5C9D5), Color(0xFFD4DDE4), Color(0xFF6B8FA8), Color(0xFF9FB8C8)),
    "feb" to listOf(Color(0xFFA88A8A), Color(0xFFD4B5B5), Color(0xFFC49A9A), Color(0xFF8A6B6B), Color(0xFFE0C5C5)),
    "mar" to listOf(Color(0xFF7A9E6B), Color(0xFFA8C498), Color(0xFF5B7F4C), Color(0xFFB5D4A5), Color(0xFF8FB87E)),
    "apr" to listOf(Color(0xFFCB6D51), Color(0xFF3B6B8A), Color(0xFF4A7C59), Color(0xFFF5E6D3), Color(0xFFC49540)),
)

private val fullYearPalettes = listOf(
    "jan" to listOf(Color(0xFF8AACBF), Color(0xFFB5C9D5), Color(0xFFD4DDE4), Color(0xFF6B8FA8), Color(0xFF9FB8C8)),
    "feb" to listOf(Color(0xFFA88A8A), Color(0xFFD4B5B5), Color(0xFFC49A9A), Color(0xFF8A6B6B), Color(0xFFE0C5C5)),
    "mar" to listOf(Color(0xFF7A9E6B), Color(0xFFA8C498), Color(0xFF5B7F4C), Color(0xFFB5D4A5), Color(0xFF8FB87E)),
    "apr" to listOf(Color(0xFFCB6D51), Color(0xFF3B6B8A), Color(0xFF4A7C59), Color(0xFFF5E6D3), Color(0xFFC49540)),
    "may" to listOf(Color(0xFFD4A24E), Color(0xFFE8C476), Color(0xFFBF8A32), Color(0xFFC49540), Color(0xFFDCB45E)),
    "jun" to listOf(Color(0xFF5B93B5), Color(0xFF85B8D4), Color(0xFF2C526B), Color(0xFF3B6B8A), Color(0xFF6BA0C0)),
    "jul" to listOf(Color(0xFFE8956A), Color(0xFFD47A4F), Color(0xFFF0A880), Color(0xFFCC6B3E), Color(0xFFE89060)),
    "aug" to listOf(Color(0xFFD4956A), Color(0xFFC48050), Color(0xFFE0A87A), Color(0xFFB87040), Color(0xFFD49868)),
    "sep" to listOf(Color(0xFF8A6B4A), Color(0xFFA88A68), Color(0xFF6B5238), Color(0xFFBFA888), Color(0xFF9E7E5A)),
    "oct" to listOf(Color(0xFFB8A9C9), Color(0xFF9882B0), Color(0xFF7B6897), Color(0xFFC4B8D6), Color(0xFFA898BC)),
    "nov" to listOf(Color(0xFF6A7A6A), Color(0xFF8A9A8A), Color(0xFF4A5A4A), Color(0xFF9AAA9A), Color(0xFF7A8A7A)),
    "dec" to listOf(Color(0xFFD4D4D4), Color(0xFFBABABA), Color(0xFFE8E8E8), Color(0xFFA0A0A0), Color(0xFFC8C8C8)),
)

// ── Preview Screen ──

@Composable
fun ShareCardPreviewScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Share Card Layouts", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(
            "final selections",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // ── WEEK/MONTH — 9:16 Story (A1) ──
        SectionHeader("Week / Month — 9:16 Story")
        CardRow {
            LayoutA1_Story("this week", "mar 31 \u2013 apr 6", "This Week")
            LayoutA1_Story("2026", "april", "This Month")
            LayoutA1_Story("a week in march", "mar 24 \u2013 30", "Earlier Week")
            LayoutA1_Story("2026", "february", "Earlier Month")
        }

        // ── WEEK/MONTH — 4:5 Feed (F2) ──
        SectionHeader("Week / Month — 4:5 Feed")
        CardRow {
            LayoutF2_Feed("this week", "mar 31 \u2013 apr 6", "This Week")
            LayoutF2_Feed("2026", "april", "This Month")
            LayoutF2_Feed("a week in march", "mar 24 \u2013 30", "Earlier Week")
            LayoutF2_Feed("2026", "february", "Earlier Month")
        }

        // ── YEAR — 9:16 Story (D1) ──
        SectionHeader("Year — 9:16 Story")
        CardRow {
            LayoutD1_Year("2026", "This Year")
            LayoutD1_Year("2024", "Earlier Year")
        }

        // ── YEAR — 4:5 Feed (D1) ──
        SectionHeader("Year — 4:5 Feed")
        CardRow {
            LayoutD1_YearFeed("2026", "This Year")
            LayoutD1_YearFeed("2024", "Earlier Year")
        }

        // ── YTD — 9:16 Story ──
        SectionHeader("Year to Date — 9:16 Story")
        CardRow {
            LayoutStackedNoLabels("2026 so far", ytdPalettes, "YTD")
        }

        // ── YEAR IN REVIEW — 9:16 Story ──
        SectionHeader("Year in Review — 9:16 Story")
        CardRow {
            LayoutStackedLabeled("2025", fullYearPalettes, "Year in Review")
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// ═══════════════════════════════════════════════════
// A1 — Week/Month story (centered, no color name list)
// ═══════════════════════════════════════════════════

@Composable
private fun LayoutA1_Story(periodType: String, dateLabel: String, label: String) {
    ShareCardFrame(9f / 16f, label) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(periodType, style = cardLabelStyle(), color = HuedTextMutedResting)
            Text(dateLabel, style = cardDateStyle(), color = HuedTextPrimary)
            Spacer(Modifier.height(32.dp))
            PaletteStripPreview(sampleColors, height = 110.dp)
            Spacer(Modifier.height(28.dp))
            Text(
                "\u201c$samplePoetic\u201d",
                style = cardPoeticStyle(),
                color = HuedTextMutedResting,
                textAlign = TextAlign.Center,
            )
        }
        WordmarkFooter()
    }
}

// ═══════════════════════════════════════════════════
// F2 — Week/Month feed (balanced, no color name list)
// ═══════════════════════════════════════════════════

@Composable
private fun LayoutF2_Feed(periodType: String, dateLabel: String, label: String) {
    ShareCardFrame(4f / 5f, label) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(periodType, style = cardLabelStyle(), color = HuedTextMutedResting)
            Text(dateLabel, style = cardDateStyle(), color = HuedTextPrimary)
            Spacer(Modifier.height(20.dp))
            PaletteStripPreview(sampleColors, height = 70.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                "\u201c$samplePoetic\u201d",
                style = cardPoeticStyle(),
                color = HuedTextMutedResting,
                textAlign = TextAlign.Center,
            )
        }
        WordmarkFooter(bottomPadding = 16.dp)
    }
}

// ═══════════════════════════════════════════════════
// D1 — Year story (large year, tall strip, stats)
// ═══════════════════════════════════════════════════

@Composable
private fun LayoutD1_Year(year: String, label: String) {
    ShareCardFrame(9f / 16f, label) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(year, style = cardYearStyle(), color = HuedTextPrimary)
            Spacer(Modifier.height(32.dp))
            PaletteStripPreview(sampleColors, height = 125.dp)
            Spacer(Modifier.height(28.dp))
            Text(
                "\u201c$samplePoetic\u201d",
                style = cardPoeticStyle(),
                color = HuedTextMutedResting,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "365 days \u00b7 1,247 images",
                style = cardLabelStyle(),
                color = HuedTextMutedResting.copy(alpha = 0.6f),
            )
        }
        WordmarkFooter()
    }
}

// D1 Feed — year for 4:5
@Composable
private fun LayoutD1_YearFeed(year: String, label: String) {
    ShareCardFrame(4f / 5f, label) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(year, style = cardHeadlineStyle(), color = HuedTextPrimary)
            Spacer(Modifier.height(20.dp))
            PaletteStripPreview(sampleColors, height = 80.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                "\u201c$samplePoetic\u201d",
                style = cardPoeticStyle(),
                color = HuedTextMutedResting,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "1,247 images",
                style = cardSmallLabelStyle(),
                color = HuedTextMutedResting.copy(alpha = 0.4f),
            )
        }
        WordmarkFooter(bottomPadding = 16.dp)
    }
}

// ═══════════════════════════════════════════════════
// Stacked — YTD + Year in Review (no month labels)
// ═══════════════════════════════════════════════════

@Composable
private fun LayoutStackedNoLabels(headline: String, palettes: List<Pair<String, List<Color>>>, label: String) {
    ShareCardFrame(9f / 16f, label) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(headline, style = cardHeadlineStyle(), color = HuedTextPrimary)
            Spacer(Modifier.height(28.dp))
            palettes.forEach { (_, colors) ->
                PaletteStripPreview(
                    colors,
                    height = if (palettes.size <= 4) 32.dp else 20.dp,
                    cornerRadius = 3.dp,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "your ${if (palettes.size <= 4) "year so far" else "year in color"}",
                style = cardTaglineStyle(),
                color = HuedTextMutedResting,
            )
        }
        // Extra bottom padding so HUED doesn't crowd the strips
        WordmarkFooter(bottomPadding = 32.dp)
    }
}

// Stacked with month labels (Year in Review) — wordmark inline, not overlaid
@Composable
private fun LayoutStackedLabeled(headline: String, palettes: List<Pair<String, List<Color>>>, label: String) {
    ShareCardFrame(9f / 16f, label) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Text(headline, style = cardHeadlineStyle(), color = HuedTextPrimary)
            Spacer(Modifier.height(24.dp))
            palettes.forEach { (month, colors) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        month,
                        style = cardSmallLabelStyle(),
                        color = HuedTextMutedResting.copy(alpha = 0.5f),
                        modifier = Modifier.width(22.dp),
                    )
                    PaletteStripPreview(colors, height = 18.dp, cornerRadius = 2.dp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "your year in color",
                style = cardTaglineStyle(),
                color = HuedTextMutedResting,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "HUED",
                style = cardWordmarkStyle(),
                color = HuedTextPrimary,
                modifier = Modifier.padding(bottom = 28.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// Shared Components
// ═══════════════════════════════════════════════════

@Composable
private fun PaletteStripPreview(
    colors: List<Color>,
    height: Dp = 80.dp,
    cornerRadius: Dp = 6.dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .background(color),
            )
        }
    }
}

@Composable
private fun WordmarkFooter(bottomPadding: Dp = 28.dp) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Text(
            "HUED",
            style = cardWordmarkStyle(),
            color = HuedTextPrimary,
            modifier = Modifier.padding(bottom = bottomPadding),
        )
    }
}

@Composable
private fun ShareCardFrame(aspectRatio: Float, label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Box(
            modifier = Modifier
                .width(180.dp)
                .aspectRatio(aspectRatio)
                .clip(RoundedCornerShape(8.dp))
                .background(HuedCanvasResting)
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        ) {
            content()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Spacer(Modifier.height(28.dp))
    Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun CardRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        content()
    }
}

// ── Typography (scaled for 180dp-wide card previews) ──

@Composable private fun cardLabelStyle() = MaterialTheme.typography.labelSmall.copy(
    fontSize = 5.sp, letterSpacing = 0.8.sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal,
)
@Composable private fun cardDateStyle() = MaterialTheme.typography.displaySmall.copy(
    fontSize = 10.sp, letterSpacing = (-0.2).sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Light,
)
@Composable private fun cardHeadlineStyle() = MaterialTheme.typography.displaySmall.copy(
    fontSize = 14.sp, letterSpacing = (-0.3).sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Light,
)
@Composable private fun cardYearStyle() = MaterialTheme.typography.displaySmall.copy(
    fontSize = 22.sp, letterSpacing = (-0.5).sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Light,
)
@Composable private fun cardPoeticStyle() = MaterialTheme.typography.bodySmall.copy(
    fontSize = 5.sp, fontStyle = FontStyle.Italic, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Light,
)
@Composable private fun cardTaglineStyle() = MaterialTheme.typography.bodySmall.copy(
    fontSize = 5.sp, letterSpacing = 0.5.sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Light,
)
@Composable private fun cardWordmarkStyle() = MaterialTheme.typography.headlineSmall.copy(
    fontSize = 7.sp, letterSpacing = 3.sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold,
)
@Composable private fun cardSmallLabelStyle() = MaterialTheme.typography.labelSmall.copy(
    fontSize = 4.sp, fontFamily = OutfitFontFamily, fontWeight = FontWeight.Light,
)
