package ru.glebik.updater.library

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import ru.glebik.updater.library.checker.CheckerParameters
import ru.glebik.updater.library.checker.UpdateCheckerWorker
import ru.glebik.updater.library.consts.InternalConsts
import ru.glebik.updater.library.init.UpdateConfig

object AutoUpdater {

    fun start(applicationContext: Context, updateConfig: UpdateConfig) {

        val inputData = checkerParamsToWorkerData(updateConfig.checkerParameters)

        if (updateConfig.isPeriodic) {
            launchPeriodicWorker(
                applicationContext = applicationContext,
                updateConfig = updateConfig,
                inputData = inputData
            )
        } else {
            launchOneTimeWorker(
                applicationContext = applicationContext,
                inputData = inputData
            )
        }
    }

    private fun checkerParamsToWorkerData(params: CheckerParameters): Data {
        val inputData = Data.Builder()
            .putString(InternalConsts.INTERNAL_KEY_FOR_CHECK_URL, params.checkUrl)
            .putString(InternalConsts.INTERNAL_KEY_FOR_KEY_APK_URL, params.keyApkUrl)
            .putString(InternalConsts.INTERNAL_KEY_FOR_KEY_VERSION, params.keyVersion)
            .putString(
                InternalConsts.INTERNAL_KEY_FOR_KEY_UPDATE_MESSAGE,
                params.keyUpdateMessage
            )
            .build()
        return inputData
    }

    private fun launchPeriodicWorker(
        applicationContext: Context,
        updateConfig: UpdateConfig,
        inputData: Data
    ) {
        val workRequest = PeriodicWorkRequest.Builder(
            UpdateCheckerWorker::class.java,
            updateConfig.repeatInterval,
            updateConfig.timeUnit
        )
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    private fun launchOneTimeWorker(
        applicationContext: Context,
        inputData: Data
    ) {
        val workRequest = OneTimeWorkRequest.Builder(UpdateCheckerWorker::class.java)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "updater_one_time_worker",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}