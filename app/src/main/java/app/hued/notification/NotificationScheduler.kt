package app.hued.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.hued.processing.ProcessingWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        const val WEEKLY_WORK_NAME = "hued_weekly_palette"
        const val MONDAY_HOUR = 9
    }

    fun scheduleWeeklyRefresh() {
        val now = LocalDateTime.now()
        val nextMonday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            .withHour(MONDAY_HOUR)
            .withMinute(0)
            .withSecond(0)

        val initialDelay = Duration.between(now, nextMonday)
        val delayMinutes = if (initialDelay.isNegative) {
            Duration.between(now, nextMonday.plusWeeks(1)).toMinutes()
        } else {
            initialDelay.toMinutes()
        }

        val workRequest = PeriodicWorkRequestBuilder<ProcessingWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }
}
