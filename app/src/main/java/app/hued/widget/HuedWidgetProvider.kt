package app.hued.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import app.hued.MainActivity
import app.hued.data.local.HuedDatabase
import app.hued.data.model.TimePeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class HuedWidget : GlanceAppWidget() {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = Room.databaseBuilder(
            context, HuedDatabase::class.java, "hued-database",
        ).build()

        val paletteEntity = withContext(Dispatchers.IO) {
            db.periodPaletteDao().getLatestByType(TimePeriod.WEEK.name).firstOrNull()
        }

        val hexColors = paletteEntity?.let {
            json.decodeFromString<List<String>>(it.colors)
        } ?: emptyList()

        val label = if (paletteEntity != null) "This week" else "hued"

        provideContent {
            WidgetContent(label = label, hexColors = hexColors)
        }
    }
}

@Composable
private fun WidgetContent(label: String, hexColors: List<String>) {
    val bgColor = ColorProvider(Color(0xFFF8F7F5))
    val textColor = ColorProvider(Color(0xFF2A2826))
    val mutedColor = ColorProvider(Color(0xFF8A8885))

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = label,
                style = TextStyle(color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Normal),
            )
            Text(
                text = "your life in color",
                style = TextStyle(color = mutedColor, fontSize = 9.sp, fontWeight = FontWeight.Normal),
            )
        }
        Spacer(modifier = GlanceModifier.width(12.dp))
        Row(modifier = GlanceModifier.defaultWeight().height(36.dp)) {
            if (hexColors.isNotEmpty()) {
                hexColors.forEach { hex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) { Color.Gray }
                    Spacer(
                        modifier = GlanceModifier.defaultWeight().height(36.dp).background(ColorProvider(color)),
                    )
                }
            } else {
                Spacer(
                    modifier = GlanceModifier.defaultWeight().height(36.dp).background(ColorProvider(Color(0xFFE0DDD8))),
                )
            }
        }
    }
}

class HuedWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HuedWidget()
}
