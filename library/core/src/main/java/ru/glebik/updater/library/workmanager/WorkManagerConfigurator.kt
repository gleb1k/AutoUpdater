package ru.glebik.updater.library.workmanager

import androidx.work.Configuration
import ru.glebik.updater.library.main.checker.UpdateCheckerWorkerFactory
import ru.glebik.updater.library.main.loader.ApkDownloader
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
 */
class DefaultWorkManagerConfigurator(
    private val apkDownloader: ApkDownloader,
    private val appVersionHelper: AppVersionHelper,
) : WorkManagerConfigurator {

    override fun createConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(UpdateCheckerWorkerFactory(apkDownloader, appVersionHelper))
            .build()
    }
}