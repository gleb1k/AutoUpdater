package ru.glebik.updater.library.main.checker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import ru.glebik.updater.library.init.UpdateConfig


interface UpdateCheckerWorkerRunner {
    fun runPeriodic(updateConfig: UpdateConfig, inputData: Data)
    fun runOneTime(inputData: Data)
}

class DefaultUpdateCheckerRunner(
    private val context: Context,
    private val factory: UpdateCheckerWorkerRequestFactory = DefaultUpdateCheckerWorkerRequestFactory()
) : UpdateCheckerWorkerRunner {

    override fun runPeriodic(updateConfig: UpdateConfig, inputData: Data) {
        val request = factory.createPeriodicRequest(updateConfig, inputData)
        WorkManager.getInstance(context).enqueue(request)
    }

    override fun runOneTime(inputData: Data) {
        val request = factory.createOneTimeRequest(inputData)
        WorkManager.getInstance(context).enqueueUniqueWork(
            UPDATE_CHECKER_WORKER_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        private const val UPDATE_CHECKER_WORKER_NAME = "UPDATER_WORKER"
    }
}