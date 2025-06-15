package ru.glebik.updater.library.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ru.glebik.updater.library.core.R

interface AutoUpdateNotifier {
    fun showUserConfirmationNotification(pendingIntent: PendingIntent)
    fun showSuccessNotification()
    fun showUpdateAvailableNotification()
    fun cancel()
}

class DefaultAutoUpdateNotifier(private val context: Context) : AutoUpdateNotifier {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.update_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.update_channel_description)
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun showUserConfirmationNotification(pendingIntent: PendingIntent) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_downloaded_title))
            .setContentText(context.getString(R.string.update_downloaded_description))
            .setAutoCancel(true)
            .setSilent(false)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun showSuccessNotification() {
        val intent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent(),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.update_success_title))
            .setContentText(context.getString(R.string.update_success_description))
            .setAutoCancel(true)
            .setSilent(false)
            .setContentIntent(intent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun showUpdateAvailableNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(context.getString(R.string.update_available_title))
            .setContentText(context.getString(R.string.update_available_description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSilent(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "auto_update_channel"
    }
}