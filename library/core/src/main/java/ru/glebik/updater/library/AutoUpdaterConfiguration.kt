package ru.glebik.updater.library

import ru.glebik.updater.library.main.checker.UpdateCheckerWorkerRunner
import ru.glebik.updater.library.main.installer.InstallerWorkerRunner
import ru.glebik.updater.library.main.loader.ApkDownloader
import ru.glebik.updater.library.notifications.AutoUpdateNotifier
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper
import ru.glebik.updater.library.workmanager.WorkManagerConfigurator

/**
 * Configuration class for customizing AutoUpdater components.
 * Use this to override default implementations with your own.
 *
 * @param notifier Handles user notifications related to the update process.
 * @param updateCheckerWorkerRunner Runs the worker that checks for updates.
 * @param installerWorkerRunner Runs the worker that installs the downloaded APK.
 * @param apkDownloader Handles downloading the APK from a remote source.
 * @param appVersionHelper Provides app version information for comparison.
 * @param workManagerConfigurator Supplies a custom WorkManager configuration.
 */
data class AutoUpdaterConfiguration(
    val notifier: AutoUpdateNotifier,
    val updateCheckerWorkerRunner: UpdateCheckerWorkerRunner,
    val installerWorkerRunner: InstallerWorkerRunner,
    val apkDownloader: ApkDownloader,
    val appVersionHelper: AppVersionHelper,
    val workManagerConfigurator: WorkManagerConfigurator,
    val prefManager: AutoUpdateSharedPrefManager,
)