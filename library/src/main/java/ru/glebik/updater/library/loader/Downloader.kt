package ru.glebik.updater.library.loader

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.database.Cursor
import android.net.Uri
import android.util.Log
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
                        updateAppWithPackageInstaller(context, getDownloadedApkFile(context))
                        context.unregisterReceiver(this)
                    }
                }
                cursor.close()
            }
        }
    }

    // Регистрируем BroadcastReceiver
    context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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

fun updateAppWithPackageInstaller(context: Context, apkFile: File) {
    try {
        Log.d("DEBUG", "File exists: ${apkFile.exists()}, path: ${apkFile.absolutePath}, size: ${apkFile.length()}\"")
        val packageManager = context.packageManager
        val packageInstaller = packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(context.packageName) // обязательно указываем имя текущего пакета
            }

        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

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
        apkFile.inputStream().use { input ->
            session.openWrite(AppUtils.getAppApkFileName(context), 0, apkFile.length()).use { output ->
                input.copyTo(output)
                session.fsync(output)
            }
        }

        // PendingIntent нужен для получения обратного вызова
        val intent = Intent(context, InstallBroadcastReceiver::class.java).apply {
            action = "com.example.ACTION_UPDATE_RESULT"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Завершаем установку
        session.commit(pendingIntent.intentSender)
        session.close()
    } catch (e: Exception) {
        Log.d("Installer", "error:$e")
    }
}

class InstallBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Log.d("Installer", "Установка прошла успешно")
            }

            else -> {
                Log.e("Installer", "Ошибка установки: $message")
            }
        }
    }
}
