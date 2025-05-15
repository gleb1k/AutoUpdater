package ru.glebik.updater.library.main.loader

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import ru.glebik.updater.library.AppUtils
import ru.glebik.updater.library.main.installer.InstallerWorker
import java.io.File


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
                        InstallerWorker.launchOneTimeWorker(context, apkUri)
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


private fun getDownloadedApkUri(context: Context): Uri {
    val apkFile = File(context.getExternalFilesDir(null), AppUtils.getAppApkFileName(context))
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
}

fun getDownloadedApkFile(context: Context): File {
    return File(context.getExternalFilesDir(null), AppUtils.getAppApkFileName(context))
}
