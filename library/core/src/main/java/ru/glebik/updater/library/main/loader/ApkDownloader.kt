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
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.core.R
import ru.glebik.updater.library.utils.AppUtils
import ru.glebik.updater.library.main.installer.InstallerWorkerRunner
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import java.io.File
import java.util.UUID

interface ApkDownloader {
    fun download(context: Context, apkUrl: String)
}

class DefaultApkDownloader(
    private val installerWorkerRunner: InstallerWorkerRunner,
    private val prefManager: AutoUpdateSharedPrefManager,
) : ApkDownloader {

    override fun download(context: Context, apkUrl: String) {

        // Получаем экземпляр DownloadManager
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        // Устанавливаем URI для скачивания
        val uri = apkUrl.toUri()
        val request = DownloadManager.Request(uri).apply {
            setTitle(context.getString(R.string.apk_download_title))
            setDescription(context.getString(R.string.apk_download_description))
            setDestinationInExternalFilesDir(context, null, AppUtils.getAppApkFileName(context))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (prefManager.isWifiOnlyEnabled) {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            } else {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }
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
                            installerWorkerRunner.runOneTime(apkUri)
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

}
