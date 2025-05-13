package ru.glebik.updater.library.loader

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.IntentCompat
import ru.glebik.updater.library.AutoUpdater

class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status =
            intent.getIntExtra(PackageInstaller.EXTRA_STATUS, UNKNOWN_PACKAGE_INSTALLER_STATUS)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        Log.d("INSTALL STATUS", status.toString())

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d("Installer", "Установка прошла успешно")
                AutoUpdater.notifier.showSuccessNotification()
                removeUpdateFileAfterInstall(context, intent)
            }

            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                getPendingIntentForInstall(context, intent)?.let {
                    showConfirmationNotification(it)
                }
            }

            else -> {
                Log.e("Installer", "Ошибка установки: $message")
                getPendingIntentForManualInstall(context, intent)?.let {
                    showConfirmationNotification(it)
                }
            }
        }
    }

    private fun removeUpdateFileAfterInstall(context: Context, intent: Intent) {
        getApkFileUri(intent)?.let { fileUri ->
            runCatching {
                context.contentResolver.delete(
                    fileUri,
                    null,
                    null
                )
            }.onFailure { Log.e("Installer", "Cannot delete update file after install") }
        }
    }

    private fun showConfirmationNotification(confirmationIntent: PendingIntent) {
        AutoUpdater.notifier.showUserConfirmationNotification(confirmationIntent)
    }

    private fun getPendingIntentForManualInstall(context: Context, intent: Intent): PendingIntent? =
        getApkFileUri(intent)?.let { uri ->
            PendingIntent.getActivity(
                context,
                0,
                Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(uri, "application/vnd.android.package-archive")
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

    private fun getPendingIntentForInstall(context: Context, intent: Intent): PendingIntent? =
        IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
            ?.let { confirmationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    confirmationIntent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

    private fun getApkFileUri(intent: Intent): Uri? {
        return IntentCompat.getParcelableExtra(intent, URI_EXTRA_KEY, Uri::class.java)
    }

    companion object {

        const val URI_EXTRA_KEY = "URI_EXTRA_KEY"
        private const val UNKNOWN_PACKAGE_INSTALLER_STATUS = -666

        fun createPendingIntent(
            context: Context,
            apkFileUri: Uri,
            installSessionId: Int,
        ): PendingIntent = PendingIntent.getBroadcast(
            context, installSessionId,
            Intent(context, InstallResultReceiver::class.java).apply {
                putExtra(URI_EXTRA_KEY, apkFileUri)
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
