package ru.glebik.updater.library.main.installer

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ru.glebik.updater.library.main.loader.ApkDownloader

class InstallerWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            val apkUri = inputData.getString(URI_KEY)?.toUri()
                ?: throw IllegalArgumentException("InstallerWorker: Input Data must have $URI_KEY parameter")
            Installer.installApk(appContext, apkUri)
        }.onFailure {
            WorkManager.getInstance(appContext).cancelWorkById(id)
            return Result.failure()
        }

        return Result.success()
    }

}