package ru.glebik.updater.library

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
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
import ru.glebik.updater.library.utils.DefaultNetworkChecker
import ru.glebik.updater.library.utils.NetworkChecker
import ru.glebik.updater.library.workmanager.DefaultWorkManagerConfigurator
import ru.glebik.updater.library.workmanager.WorkManagerConfigurator
import java.util.UUID


object AutoUpdater {

    private lateinit var applicationContext: Context

    private lateinit var updateCheckerWorkerRunner: UpdateCheckerWorkerRunner

    private lateinit var apkDownloader: ApkDownloader

    private lateinit var workManagerConfigurator: WorkManagerConfigurator

    lateinit var notifier: AutoUpdateNotifier
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
     * @param configuration Configuration containing user-defined implementations of core components.
     */
    fun init(
        applicationContext: Context,
        configuration: AutoUpdaterConfiguration,
    ) {

        this.applicationContext = applicationContext.applicationContext
        this.notifier = configuration.notifier
        this.updateCheckerWorkerRunner = configuration.updateCheckerWorkerRunner
        this.apkDownloader = configuration.apkDownloader
        this.workManagerConfigurator = configuration.workManagerConfigurator
        this.appVersionHelper = configuration.appVersionHelper
        this.prefManager = configuration.prefManager
        this.networkChecker = configuration.networkChecker

        WorkManager.initialize(
            applicationContext,
            configuration.workManagerConfigurator.createConfiguration()
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
            DefaultApkDownloader(defaultInstallerWorkerRunner, defaultAutoUpdateSharedPrefManager)

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

    fun checkUpdate(parameters: CheckerParameters): UUID {
        val inputData = CheckerParamsMapper.map(
            parameters,
            false
        )

        return when (parameters) {
            is CheckerParameters.OneTime -> updateCheckerWorkerRunner.runOneTime(inputData)
            is CheckerParameters.Periodic -> updateCheckerWorkerRunner.runPeriodic(
                parameters,
                inputData
            )
        }
    }

    fun checkAndInstallUpdate(checkerParameters: CheckerParameters): UUID {
        val inputData = CheckerParamsMapper.map(
            checkerParameters,
            true
        )

        return when (checkerParameters) {
            is CheckerParameters.OneTime -> updateCheckerWorkerRunner.runOneTime(inputData)
            is CheckerParameters.Periodic -> updateCheckerWorkerRunner.runPeriodic(
                checkerParameters,
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