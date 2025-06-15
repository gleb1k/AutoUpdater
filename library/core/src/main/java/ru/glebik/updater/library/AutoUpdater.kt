package ru.glebik.updater.library

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
import ru.glebik.updater.library.init.UpdateConfig
import ru.glebik.updater.library.main.checker.CheckerParameters
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
import ru.glebik.updater.library.utils.NetworkChecker
import ru.glebik.updater.library.utils.DefaultNetworkChecker
import ru.glebik.updater.library.workmanager.DefaultWorkManagerConfigurator
import ru.glebik.updater.library.workmanager.WorkManagerConfigurator
import java.util.UUID


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

    lateinit var networkChecker: NetworkChecker
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
        this.networkChecker = autoUpdaterConfiguration.networkChecker

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

        val defaultAutoUpdateSharedPrefManager =
            DefaultAutoUpdateSharedPrefManager(applicationContext)

        val defaultApkDownloader =
            DefaultApkDownloader(defaultInstallerWorkerRunner,defaultAutoUpdateSharedPrefManager )

        val appVersionHelper = AppVersionHelper(applicationContext)

        val defaultWorkManagerConfigurator =
            DefaultWorkManagerConfigurator(
                defaultApkDownloader,
                appVersionHelper,
                defaultAutoUpdateSharedPrefManager,
                defaultNotifier,
            )

        val defaultNetworkChecker = DefaultNetworkChecker(applicationContext)

        this.applicationContext = applicationContext.applicationContext
        this.notifier = defaultNotifier
        this.updateCheckerWorkerRunner = defaultUpdateCheckerWorkerRunner
        this.apkDownloader = defaultApkDownloader
        this.workManagerConfigurator = defaultWorkManagerConfigurator
        this.appVersionHelper = appVersionHelper
        this.prefManager = defaultAutoUpdateSharedPrefManager
        this.networkChecker = defaultNetworkChecker

        WorkManager.initialize(
            applicationContext,
            defaultWorkManagerConfigurator.createConfiguration()
        )
    }

    fun checkUpdate(updateConfig: UpdateConfig): UUID {
        val inputData = CheckerParamsMapper.map(
            updateConfig.checkerParameters,
            updateConfig.needToDownloadAfterCheck
        )

        return when (updateConfig.checkerParameters) {
            is CheckerParameters.OneTime -> updateCheckerWorkerRunner.runOneTime(inputData)
            is CheckerParameters.Periodic -> updateCheckerWorkerRunner.runPeriodic(
                updateConfig.checkerParameters,
                inputData
            )
        }
    }

    fun downloadAndInstallAvailableUpdate() {
        prefManager.availableUpdate?.let {
            apkDownloader.download(applicationContext, it.apkUrl)
        }
    }
}