package ru.glebik.updater.library.main.installer

import android.content.Context
import android.net.Uri
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager

interface InstallerWorkerRunner {
    fun runOneTime(apkUri: Uri)
}

class DefaultInstallerWorkerRunner(
    private val context: Context,
    private val factory: InstallerWorkerRequestFactory = DefaultInstallerWorkerRequestFactory()
) : InstallerWorkerRunner {

    override fun runOneTime(apkUri: Uri) {
        val request = factory.createOneTimeRequest(apkUri)
        WorkManager.getInstance(context).enqueueUniqueWork(
            INSTALLER_WORKER_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        private const val INSTALLER_WORKER_NAME = "INSTALLER_WORKER"
    }
}