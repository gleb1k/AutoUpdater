package ru.glebik.updater.library.loader

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ru.glebik.updater.library.AppUtils
import java.io.File


fun downloadApk(context: Context, apkUrl: String) {

    // Получаем экземпляр DownloadManager
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    // Устанавливаем URI для скачивания
    val uri = Uri.parse(apkUrl)
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
                        installApk(context, apkUri)
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

fun installApk(context: Context, apkUri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}