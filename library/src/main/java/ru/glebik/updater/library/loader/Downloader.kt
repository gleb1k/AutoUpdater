package ru.glebik.updater.library.loader

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import ru.glebik.updater.library.AppUtils
import java.io.File
import java.util.zip.ZipFile


fun downloadApk(context: Context, apkUrl: String) {

    // Получаем экземпляр DownloadManager
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    // Устанавливаем URI для скачивания
    val uri = apkUrl.toUri()
    val request = DownloadManager.Request(uri).apply {
        setTitle("Downloading APK")
        setDescription("Downloading the latest version of the app.")
        setDestinationInExternalFilesDir(context, null, AppUtils.getAppApkFileName(context))
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    }

    // Загружаем файл
    val downloadId = downloadManager.enqueue(request)

    // Регистрируем BroadcastReceiver для отслеживания завершения загрузки
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                val query = DownloadManager.Query().apply {
                    setFilterById(downloadId)
                }
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        // Установка APK
                        val apkUri = getDownloadedApkUri(context)
                        updateAppWithPackageInstaller(
                            context,
                            apkUri
                        )
                        //installApk(context, apkUri)
                        context.unregisterReceiver(this)
                    }
                }
                cursor.close()
            }
        }
    }

    // Регистрируем BroadcastReceiver
    ContextCompat.registerReceiver(
        context,
        receiver,
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        ContextCompat.RECEIVER_EXPORTED
    )
}

fun getDownloadedApkUri(context: Context): Uri {
    val apkFile = File(context.getExternalFilesDir(null), AppUtils.getAppApkFileName(context))
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
}

fun getDownloadedApkFile(context: Context): File {
    return File(context.getExternalFilesDir(null), AppUtils.getAppApkFileName(context))
}

fun installApk(context: Context, apkUri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
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

