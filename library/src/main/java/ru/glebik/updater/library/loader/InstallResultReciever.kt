package ru.glebik.updater.library.loader

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        Log.d("INSTALL STATUS", status.toString())

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d("Installer", "Установка прошла успешно")
            }


            else -> {
                Log.e("Installer", "Ошибка установки: $message")
            }
        }
    }

    companion object {
        fun createPendingIntent(
            context: Context,
            apkFileUri: Uri,
            installSessionId: Int,
        ): PendingIntent = PendingIntent.getBroadcast(
            context, installSessionId,
            Intent(context, InstallResultReceiver::class.java).apply {
//                putExtra(
//                    UPDATE_APK_FILE_URI_EXTRA,
//                    apkFileUri
//                )
            },
            getResultIntentFlags(),
        )

        private fun getResultIntentFlags(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
            } else {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            }
    }
}
