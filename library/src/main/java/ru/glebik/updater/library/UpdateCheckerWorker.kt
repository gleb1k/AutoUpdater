package ru.glebik.updater.library

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.glebik.updater.library.consts.Consts
import ru.glebik.updater.library.http.HttpUtils
import ru.glebik.updater.library.parser.Parser

class UpdateCheckerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val checkUrl = inputData.getString(Consts.KEY_CHECK_URL)
                ?: throw IllegalArgumentException("Input Data must have ${Consts.KEY_CHECK_URL} parameter")

            val response = HttpUtils.get(checkUrl) ?: return Result.failure()
            val checkModel = Parser.parseJson(response)
            Log.d(Consts.LIBRARY_TAG, checkModel?.toString() ?: "no response")

            downloadApk(applicationContext, "https://raw.githubusercontent.com/feicien/android-auto-update/develop/extras/android-auto-update-v1.3.apk")

            Result.success()
        } catch (e: Exception) {
            Log.e(Consts.LIBRARY_TAG, e.toString())
            Result.failure()
        }
    }
}