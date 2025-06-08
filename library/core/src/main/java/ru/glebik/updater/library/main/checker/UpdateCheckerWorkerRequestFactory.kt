package ru.glebik.updater.library.main.checker

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import ru.glebik.updater.library.init.UpdateConfig

interface UpdateCheckerWorkerRequestFactory {
    fun createPeriodicRequest(
        config: UpdateConfig,
        inputData: Data
    ): PeriodicWorkRequest

    fun createOneTimeRequest(
        inputData: Data
    ): OneTimeWorkRequest
}

class DefaultUpdateCheckerWorkerRequestFactory : UpdateCheckerWorkerRequestFactory {

    override fun createPeriodicRequest(
        config: UpdateConfig,
        inputData: Data
    ): PeriodicWorkRequest {
        return PeriodicWorkRequest.Builder(
            UpdateCheckerWorker::class.java,
            config.repeatInterval,
            config.timeUnit
        )
            .setInputData(inputData)
            .build()
    }

    override fun createOneTimeRequest(inputData: Data): OneTimeWorkRequest {
        return OneTimeWorkRequest.Builder(UpdateCheckerWorker::class.java)
            .setInputData(inputData)
            .build()
    }
}