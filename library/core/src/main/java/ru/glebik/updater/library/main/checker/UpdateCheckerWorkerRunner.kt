package ru.glebik.updater.library.main.checker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import java.util.UUID


interface UpdateCheckerWorkerRunner {
    fun runPeriodic(periodicCheckerParameters: CheckerParameters.Periodic, inputData: Data): UUID
    fun runOneTime(inputData: Data): UUID
}

class DefaultUpdateCheckerRunner(
    private val context: Context,
    private val factory: UpdateCheckerWorkerRequestFactory = DefaultUpdateCheckerWorkerRequestFactory()
) : UpdateCheckerWorkerRunner {

    override fun runPeriodic(
        periodicCheckerParameters: CheckerParameters.Periodic,
        inputData: Data
    ): UUID {
        val request = factory.createPeriodicRequest(periodicCheckerParameters, inputData)
        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }

    override fun runOneTime(inputData: Data): UUID {
        val request = factory.createOneTimeRequest(inputData)
        WorkManager.getInstance(context).enqueueUniqueWork(
            UPDATE_CHECKER_WORKER_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        return request.id
    }

    companion object {
        private const val UPDATE_CHECKER_WORKER_NAME = "UPDATER_WORKER"
    }
}