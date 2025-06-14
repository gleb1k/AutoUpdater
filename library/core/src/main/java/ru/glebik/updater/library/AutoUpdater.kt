package ru.glebik.updater.library

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
import ru.glebik.updater.library.init.UpdateConfig
import ru.glebik.updater.library.main.checker.DefaultUpdateCheckerRunner
import ru.glebik.updater.library.main.checker.DefaultUpdateCheckerWorkerRequestFactory
import ru.glebik.updater.library.main.checker.UpdateCheckerWorkerRunner
import ru.glebik.updater.library.main.installer.DefaultInstallerWorkerRequestFactory
import ru.glebik.updater.library.main.installer.DefaultInstallerWorkerRunner
import ru.glebik.updater.library.main.loader.ApkDownloader
import ru.glebik.updater.library.main.loader.DefaultApkDownloader
import ru.glebik.updater.library.models.mapper.CheckerParamsMapper
import ru.glebik.updater.library.notifications.AutoUpdateNotifier
import ru.glebik.updater.library.notifications.DefaultAutoUpdateNotifier
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.pref.DefaultAutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper
import ru.glebik.updater.library.workmanager.DefaultWorkManagerConfigurator
import ru.glebik.updater.library.workmanager.WorkManagerConfigurator


object AutoUpdater {

    lateinit var applicationContext: Context
        private set

    lateinit var notifier: AutoUpdateNotifier
        private set

    lateinit var updateCheckerWorkerRunner: UpdateCheckerWorkerRunner
        private set

    lateinit var apkDownloader: ApkDownloader
        private set

    lateinit var workManagerConfigurator: WorkManagerConfigurator
        private set

    @SuppressLint("StaticFieldLeak")
    lateinit var appVersionHelper: AppVersionHelper
        private set

    @SuppressLint("StaticFieldLeak")
    lateinit var prefManager: AutoUpdateSharedPrefManager
        private set

    /**
     * Initializes the AutoUpdater with custom implementations.
     * Must be called from Application.onCreate.
     *
     * @param applicationContext The application context.
     * @param autoUpdaterConfiguration Configuration containing user-defined implementations of core components.
     */
    fun init(
        applicationContext: Context,
        autoUpdaterConfiguration: AutoUpdaterConfiguration,
    ) {

        this.applicationContext = applicationContext.applicationContext
        this.notifier = autoUpdaterConfiguration.notifier
        this.updateCheckerWorkerRunner = autoUpdaterConfiguration.updateCheckerWorkerRunner
        this.apkDownloader = autoUpdaterConfiguration.apkDownloader
        this.workManagerConfigurator = autoUpdaterConfiguration.workManagerConfigurator
        this.appVersionHelper = autoUpdaterConfiguration.appVersionHelper
        this.prefManager = autoUpdaterConfiguration.prefManager

        WorkManager.initialize(
            applicationContext,
            autoUpdaterConfiguration.workManagerConfigurator.createConfiguration()
        )
    }

    /**
     * Initializes the AutoUpdater with default implementations.
     * Must be called from Application.onCreate.
     *
     * @param applicationContext The application context.
     */
    fun init(
        applicationContext: Context,
    ) {
        val defaultNotifier = DefaultAutoUpdateNotifier(applicationContext)

        val defaultUpdateCheckerWorkerRequestFactory = DefaultUpdateCheckerWorkerRequestFactory()
        val defaultUpdateCheckerWorkerRunner =
            DefaultUpdateCheckerRunner(applicationContext, defaultUpdateCheckerWorkerRequestFactory)

        val defaultInstallerWorkerRequestFactory = DefaultInstallerWorkerRequestFactory()
        val defaultInstallerWorkerRunner =
            DefaultInstallerWorkerRunner(applicationContext, defaultInstallerWorkerRequestFactory)

        val defaultApkDownloader =
            DefaultApkDownloader(defaultInstallerWorkerRunner)

        val appVersionHelper = AppVersionHelper(applicationContext)

        val defaultAutoUpdateSharedPrefManager =
            DefaultAutoUpdateSharedPrefManager(applicationContext)

        val defaultWorkManagerConfigurator =
            DefaultWorkManagerConfigurator(
                DefaultApkDownloader(defaultInstallerWorkerRunner),
                appVersionHelper,
                defaultAutoUpdateSharedPrefManager,
            )

        this.applicationContext = applicationContext.applicationContext
        this.notifier = defaultNotifier
        this.updateCheckerWorkerRunner = defaultUpdateCheckerWorkerRunner
        this.apkDownloader = defaultApkDownloader
        this.workManagerConfigurator = defaultWorkManagerConfigurator
        this.appVersionHelper = appVersionHelper
        this.prefManager = defaultAutoUpdateSharedPrefManager

        WorkManager.initialize(
            applicationContext,
            defaultWorkManagerConfigurator.createConfiguration()
        )
    }

    fun checkUpdate(updateConfig: UpdateConfig) {
        val inputData = CheckerParamsMapper.map(updateConfig.checkerParameters)

        if (updateConfig.isPeriodic) {
            updateCheckerWorkerRunner.runPeriodic(updateConfig, inputData)
        } else {
            updateCheckerWorkerRunner.runOneTime(inputData)
        }
    }

    fun downloadAndInstallAvailableUpdate() {
        prefManager.availableUpdate?.let {
            apkDownloader.download(applicationContext, it.apkUrl)
        }
    }
}