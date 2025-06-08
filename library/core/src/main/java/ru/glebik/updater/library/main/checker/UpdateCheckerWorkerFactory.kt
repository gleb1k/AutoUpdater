package ru.glebik.updater.library.main.checker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.glebik.updater.library.main.loader.ApkDownloader


class UpdateCheckerWorkerFactory(
    private val apkDownloader: ApkDownloader
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        return if (workerClassName == UpdateCheckerWorker::class.java.name) {
            UpdateCheckerWorker(appContext, workerParameters, apkDownloader)
        } else null
    }
}