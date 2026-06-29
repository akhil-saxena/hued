package app.hued.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
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
        const val WEEKLY_PROCESS_WORK_NAME = "hued_weekly_process"
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

        // Chain: ProcessingWorker (scan new photos) → MondayNotificationWorker (notify + update widget)
        val processWork = OneTimeWorkRequestBuilder<ProcessingWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        val notifyWork = PeriodicWorkRequestBuilder<MondayNotificationWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        // Enqueue the periodic notification worker. UPDATE (not KEEP) so schedule changes in future
        // app versions actually take effect on existing installs.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            notifyWork,
        )

        // Also enqueue a one-time processing worker aligned to the same schedule. Unique + REPLACE so
        // we don't stack a new delayed worker every time processing starts.
        WorkManager.getInstance(context).enqueueUniqueWork(
            WEEKLY_PROCESS_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            processWork,
        )
    }
}
