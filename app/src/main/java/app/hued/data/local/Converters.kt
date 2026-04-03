package app.hued.data.local

import androidx.room.TypeConverter
import app.hued.data.model.TimePeriod
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromColorList(colors: String): List<String> = json.decodeFromString(colors)

    @TypeConverter
    fun toColorList(colors: List<String>): String = json.encodeToString(colors)

    @TypeConverter
    fun fromTimePeriod(period: TimePeriod): String = period.name

    @TypeConverter
    fun toTimePeriod(value: String): TimePeriod = TimePeriod.valueOf(value)

    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
}
