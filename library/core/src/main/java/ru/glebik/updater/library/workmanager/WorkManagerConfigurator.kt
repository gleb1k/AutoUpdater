package ru.glebik.updater.library.workmanager

import androidx.work.Configuration
import ru.glebik.updater.library.main.checker.UpdateCheckerWorkerFactory
import ru.glebik.updater.library.main.loader.ApkDownloader
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper

/**
 * Provides WorkManager Configuration with custom WorkerFactory.
 * Used to configure WorkManager initialization.
 */
interface WorkManagerConfigurator {
    fun createConfiguration(): Configuration
}

/**
 * Default implementation of [WorkManagerConfigurator].
 * Supplies a WorkerFactory with required dependencies for workers.
 *
 * @param apkDownloader Used by UpdateCheckerWorker to download updates.
 * @param appVersionHelper Provides version comparison logic for updates.
 *  @param prefManager Provides variables with autoUpdate info.
 */
class DefaultWorkManagerConfigurator(
    private val apkDownloader: ApkDownloader,
    private val appVersionHelper: AppVersionHelper,
    private val prefManager: AutoUpdateSharedPrefManager,
) : WorkManagerConfigurator {

    override fun createConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(UpdateCheckerWorkerFactory(apkDownloader, appVersionHelper, prefManager))
            .build()
    }
}