package ru.glebik.updater.library.workmanager

import androidx.work.Configuration
import ru.glebik.updater.library.main.checker.UpdateCheckerWorkerFactory
import ru.glebik.updater.library.main.loader.ApkDownloader

interface WorkManagerConfigurator {
    fun createConfiguration(): Configuration
}

class DefaultWorkManagerConfigurator(
    private val apkDownloader: ApkDownloader
) : WorkManagerConfigurator {
    override fun createConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(UpdateCheckerWorkerFactory(apkDownloader))
            .build()
    }
}