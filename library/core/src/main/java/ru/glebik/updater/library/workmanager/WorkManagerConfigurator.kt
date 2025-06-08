package ru.glebik.updater.library.workmanager

import androidx.work.Configuration
import ru.glebik.updater.library.main.checker.UpdateCheckerWorkerFactory
import ru.glebik.updater.library.main.loader.ApkDownloader
import ru.glebik.updater.library.utils.AppVersionHelper

interface WorkManagerConfigurator {
    fun createConfiguration(): Configuration
}

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