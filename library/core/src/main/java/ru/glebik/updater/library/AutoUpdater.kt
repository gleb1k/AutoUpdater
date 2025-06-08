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

    // Метод инициализации с возможностью передать кастомные реализации
//    fun initCustom(
//        applicationContext: Context,
//        notifier: AutoUpdateNotifier = DefaultAutoUpdateNotifier(applicationContext),
//        updateCheckerWorkerRunner: UpdateCheckerWorkerRunner = DefaultUpdateCheckerRunner(
//            applicationContext
//        ),
//        apkDownloader: ApkDownloader = DefaultApkDownloader(
//            DefaultInstallerWorkerRunner(
//                applicationContext
//            )
//        ),
//    ) {
//        this.applicationContext = applicationContext.applicationContext
//        this.notifier = notifier
//        this.updateCheckerWorkerRunner = updateCheckerWorkerRunner
//        this.apkDownloader = apkDownloader
//
//
//        val delegationWorkerFactory = DelegatingWorkerFactory().apply {
//            addFactory()
//        }
//
//        val config = Configuration.Builder()
//            .setWorkerFactory(delegationWorkerFactory)
//            .build()
//
//        WorkManager.initialize(applicationContext, config)
//    }

    //Invoke in Application.OnCreate
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

        val defaultWorkManagerConfigurator =
            DefaultWorkManagerConfigurator(
                DefaultApkDownloader(defaultInstallerWorkerRunner),
                appVersionHelper
            )

        this.applicationContext = applicationContext.applicationContext
        this.notifier = defaultNotifier
        this.updateCheckerWorkerRunner = defaultUpdateCheckerWorkerRunner
        this.apkDownloader = defaultApkDownloader
        this.workManagerConfigurator = defaultWorkManagerConfigurator
        this.appVersionHelper = appVersionHelper

        WorkManager.initialize(
            applicationContext,
            defaultWorkManagerConfigurator.createConfiguration()
        )
    }

    fun startInstallProcess(updateConfig: UpdateConfig) {
        val inputData = CheckerParamsMapper.map(updateConfig.checkerParameters)

        if (updateConfig.isPeriodic) {
            updateCheckerWorkerRunner.runPeriodic(updateConfig, inputData)
        } else {
            updateCheckerWorkerRunner.runOneTime(inputData)
        }
    }
}