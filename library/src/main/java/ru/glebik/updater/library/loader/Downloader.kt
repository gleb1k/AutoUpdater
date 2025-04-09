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
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import ru.glebik.updater.library.AppUtils
import java.io.File


fun downloadApk(context: Context, apkUrl: String) {

    // Получаем экземпляр DownloadManager
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    // Устанавливаем URI для скачивания
    val uri = apkUrl.toUri()
    val request = DownloadManager.Request(uri).apply {
        setTitle("Downloading APK")
        setDescription("Downloading the latest version of the app.")
        setDestinationInExternalFilesDir(context, null, AppUtils.getAppFileName(context))
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
    val apkFile = File(context.getExternalFilesDir(null), AppUtils.getAppFileName(context))
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
}

fun getDownloadedApkFile(context: Context): File {
     return File(context.getExternalFilesDir(null), AppUtils.getAppFileName(context))
}

fun installApk(context: Context, apkUri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}

fun updateAppWithPackageInstaller(context: Context, apkFile: File) {
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
        setAppPackageName(context.packageName) // обязательно указываем имя текущего пакета
    }

    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)

    // Пишем APK в сессию
    apkFile.inputStream().use { input ->
        session.openWrite("update", 0, -1).use { output ->
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
