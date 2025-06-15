package ru.glebik.updater.library.main.checker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.glebik.updater.library.main.loader.ApkDownloader
import ru.glebik.updater.library.notifications.AutoUpdateNotifier
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper


class UpdateCheckerWorkerFactory(
    private val apkDownloader: ApkDownloader,
    private val appVersionHelper: AppVersionHelper,
    private val prefManager: AutoUpdateSharedPrefManager,
    private val notifier: AutoUpdateNotifier,
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        return if (workerClassName == UpdateCheckerWorker::class.java.name) {
            UpdateCheckerWorker(appContext, workerParameters, apkDownloader, appVersionHelper, prefManager, notifier)
        } else null
    }
}