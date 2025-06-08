package ru.glebik.updater.library.main.installer

import android.net.Uri
import androidx.work.OneTimeWorkRequest
import androidx.work.workDataOf


interface InstallerWorkerRequestFactory {
    fun createOneTimeRequest(apkUri: Uri): OneTimeWorkRequest
}

class DefaultInstallerWorkerRequestFactory : InstallerWorkerRequestFactory {
    override fun createOneTimeRequest(apkUri: Uri): OneTimeWorkRequest {
        return OneTimeWorkRequest.Builder(InstallerWorker::class.java)
            .setInputData(workDataOf(URI_KEY to apkUri.toString()))
            .build()
    }
}

const val URI_KEY = "URI_KEY"
