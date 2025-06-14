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

    private fun getDownloadedApkFile(context: Context): File {
        return File(context.getExternalFilesDir(null), AppUtils.getAppApkFileName(context))
    }

    fun updateAppWithPackageInstaller(context: Context, apkFileUri: Uri) {
        try {
            val apkFile = getDownloadedApkFile(context)
            Log.d(
                "DEBUG",
                "File exists: ${apkFile.exists()}, path: ${apkFile.absolutePath}, size: ${apkFile.length()}\""
            )
            val packageManager = context.packageManager
            val packageInstaller = packageManager.packageInstaller

            val installSessionId =
                packageInstaller.createSession(configureInstallSessionParams(context))
            val installSession = packageInstaller.openSession(installSessionId)

            val contentResolver = context.contentResolver

            val info = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (info == null) {
                Log.e("DEBUG", "Invalid APK file: ${apkFile.absolutePath}")
                return
            }

            val zipFile = ZipFile(apkFile)
            val entry = zipFile.getEntry("AndroidManifest.xml")
            if (entry != null) {
                Log.d("DEBUG", "APK is valid!")
            } else {
                Log.e("DEBUG", "Invalid APK format")
            }

            // Пишем APK в сессию
            contentResolver.openInputStream(apkFileUri).use { apkStream ->
                requireNotNull(apkStream) { "$apkFileUri openInputStream was null!" }
                val installSessionStream =
                    installSession.openWrite("INSTALL_SESSION_FILE", 0, -1)
                installSessionStream.buffered().use { bufferedInstallStream ->
                    apkStream.copyTo(bufferedInstallStream)
                    bufferedInstallStream.flush()
                    installSession.fsync(installSessionStream)
                }
            }

            val resultPendingIntent = InstallResultReceiver.createPendingIntent(
                context = context, apkFileUri = apkFileUri, installSessionId = installSessionId
            )

            // Завершаем установку
            installSession.commit(resultPendingIntent.intentSender)
            installSession.close()
            AutoUpdater.prefManager.availableUpdate = null
        } catch (e: Exception) {
            Log.d("Installer", "error:$e")
        }
    }

    private fun configureInstallSessionParams(context: Context): PackageInstaller.SessionParams =
        PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setInstallerPackageName(context.applicationContext.packageName)
            }
            setOriginatingUid(Process.myUid())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                setInstallReason(PackageManager.INSTALL_REASON_USER)
            }
            setAppPackageName(context.applicationContext.packageName)
        }
}