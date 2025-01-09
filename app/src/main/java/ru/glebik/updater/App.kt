package ru.glebik.updater

import android.app.Application
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.glebik.updater.library.UpdateCheckerWorker
import ru.glebik.updater.library.consts.Consts


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val inputData = Data.Builder()
            .putString(Consts.KEY_CHECK_URL, Consts.CHECK_URL)
            .build()
        // Настройка и запуск Worker
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(UpdateCheckerWorker::class.java)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
    }
}