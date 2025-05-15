package ru.glebik.updater.library.main.installer

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class InstallerWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        kotlin.runCatching {
            val apkUri = inputData.getString(URI_KEY)?.toUri()
                ?: throw IllegalArgumentException("InstallerWorker: Input Data must have $URI_KEY parameter")
            Installer.updateAppWithPackageInstaller(appContext, apkUri)
        }.onFailure {
            WorkManager.getInstance(appContext).cancelWorkById(id)
            return Result.failure()
        }

        return Result.success()
    }

    companion object {

        private const val URI_KEY = "URI_KEY"

        fun launchOneTimeWorker(
            applicationContext: Context,
            apkUri: Uri
        ) {
            val workRequest = OneTimeWorkRequest.Builder(InstallerWorker::class.java)
                .setInputData(workDataOf(URI_KEY to apkUri.toString()))
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "INSTALLER_WORKER",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}