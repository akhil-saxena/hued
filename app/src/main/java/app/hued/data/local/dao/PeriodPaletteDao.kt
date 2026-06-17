package app.hued.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.hued.data.local.entity.PeriodPaletteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodPaletteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(palette: PeriodPaletteEntity)

    @Query("SELECT * FROM PeriodPalette WHERE periodType = :type ORDER BY startDate DESC")
    fun getAllByType(type: String): Flow<List<PeriodPaletteEntity>>

    @Query("SELECT * FROM PeriodPalette WHERE periodType = :type AND startDate = :startDate AND endDate = :endDate LIMIT 1")
    suspend fun getForPeriod(type: String, startDate: Long, endDate: Long): PeriodPaletteEntity?

    @Query("SELECT * FROM PeriodPalette WHERE periodType = :type ORDER BY startDate DESC LIMIT 1")
    fun getLatestByType(type: String): Flow<PeriodPaletteEntity?>

    @Query("SELECT * FROM PeriodPalette WHERE periodType = :type AND startDate = :startDate LIMIT 1")
    suspend fun getByStart(type: String, startDate: Long): PeriodPaletteEntity?

    @Query("SELECT startDate FROM PeriodPalette WHERE periodType = 'WEEK' ORDER BY startDate DESC")
    suspend fun getWeekStartDates(): List<Long>

    @Query("SELECT COUNT(*) FROM PeriodPalette WHERE periodType = 'WEEK'")
    suspend fun getWeekCount(): Int

    // Sum the photo counts of the matching week palette(s). COUNT(*) would only ever return 0/1
    // (one row per week), which made the weekly-notification threshold impossible to reach.
    @Query("SELECT COALESCE(SUM(photoCount), 0) FROM PeriodPalette WHERE periodType = 'WEEK' AND startDate >= :startDate AND endDate <= :endDate")
    suspend fun getPhotoCountForPeriod(startDate: Long, endDate: Long): Int

    @Query("SELECT dominantColor FROM PeriodPalette GROUP BY dominantColor ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getFavoriteColor(): String?

    @Query("DELETE FROM PeriodPalette WHERE periodType = :type AND startDate = :startDate")
    suspend fun deleteForPeriod(type: String, startDate: Long)

    @Query("DELETE FROM PeriodPalette")
    suspend fun deleteAll()
}
