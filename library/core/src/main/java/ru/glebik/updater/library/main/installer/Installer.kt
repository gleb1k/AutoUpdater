package ru.glebik.updater.library.main.installer

import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.util.Log
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.utils.AppUtils
import java.io.File
import java.util.zip.ZipFile

object Installer {

    fun installApk(context: Context, apkUri: Uri) {
        runCatching {
            val installer = context.packageManager.packageInstaller
            val sessionId = installer.createSession(createSessionParams(context))
            installer.openSession(sessionId).use { session ->
                writeApkToSession(context, apkUri, session)
                val intentSender = InstallResultReceiver.createPendingIntent(
                    context = context,
                    apkFileUri = apkUri,
                    installSessionId = sessionId
                ).intentSender
                session.commit(intentSender)
            }
            AutoUpdater.prefManager.availableUpdate = null
        }.onFailure {
            Log.e("Installer", "Installation failed", it)
        }
    }

    private fun writeApkToSession(context: Context, apkUri: Uri, session: PackageInstaller.Session) {
        val inputStream = context.contentResolver.openInputStream(apkUri)
            ?: error("Cannot open APK stream from URI: $apkUri")

        inputStream.buffered().use { apkStream ->
            session.openWrite("apk", 0, -1).buffered().use { sessionStream ->
                apkStream.copyTo(sessionStream)
                sessionStream.flush()
                session.fsync(sessionStream)
            }
        }
    }

    private fun createSessionParams(context: Context): PackageInstaller.SessionParams {
        return PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setOriginatingUid(Process.myUid())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                setInstallReason(PackageManager.INSTALL_REASON_USER)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setInstallerPackageName(context.packageName)
            }
        }
    }
}