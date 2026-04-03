package app.hued.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.hued.MainActivity
import app.hued.R
import app.hued.data.repository.PaletteRepository
import app.hued.util.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.ZoneId

private const val CHANNEL_ID = "hued_weekly"
private const val NOTIFICATION_ID = 100
private const val MIN_PHOTO_THRESHOLD = 5

@HiltWorker
class MondayNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val paletteRepository: PaletteRepository,
    private val notificationPermissionHelper: NotificationPermissionHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!notificationPermissionHelper.hasNotificationPermission()) return Result.success()

        val startOfWeek = DateUtils.startOfWeek()
        val endOfWeek = DateUtils.endOfWeek()

        val photoCount = paletteRepository.getPhotoCountForPeriod(
            startOfWeek.toEpochDay(),
            endOfWeek.toEpochDay(),
        )

        // Suppress if below threshold — silence, not guilt
        if (photoCount < MIN_PHOTO_THRESHOLD) return Result.success()

        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val context = applicationContext
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_weekly", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_share)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_weekly_title))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Weekly Palette",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Your weekly color palette notification"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
