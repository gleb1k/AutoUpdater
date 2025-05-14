package ru.glebik.updater.library.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat

class AutoUpdateNotifier(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления от системы автообновлений"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)

        }
        notificationManager.createNotificationChannel(channel)
    }


    fun showUserConfirmationNotification(pendingIntent: PendingIntent) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Скачана новая версия")
            .setContentText("Для установки необходимо будет дать разрешения")
            .setAutoCancel(true)
            .setSilent(false)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showSuccessNotification() {
        val intent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent(),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Обновление завершено")
            .setContentText("Приложение успешно обновлено.")
            .setAutoCancel(true)
            .setSilent(false)
            .setContentIntent(intent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "auto_update_channel"
        private const val CHANNEL_NAME = "Auto Update Notifications"

    }
}