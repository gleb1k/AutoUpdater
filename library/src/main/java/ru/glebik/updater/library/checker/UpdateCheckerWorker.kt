package ru.glebik.updater.library.checker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ru.glebik.updater.library.consts.InternalConsts
import ru.glebik.updater.library.http.HttpUtils
import ru.glebik.updater.library.loader.downloadApk
import ru.glebik.updater.library.parser.Parser
import ru.glebik.updater.library.parser.ParserParameters

class UpdateCheckerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val checkUrl = inputData.getString(InternalConsts.INTERNAL_KEY_FOR_CHECK_URL)
                ?: throw IllegalArgumentException("Input Data must have ${InternalConsts.INTERNAL_KEY_FOR_CHECK_URL} parameter")
            val keyApkUrl = inputData.getString(InternalConsts.INTERNAL_KEY_FOR_KEY_APK_URL)
                ?: throw IllegalArgumentException("Input Data must have ${InternalConsts.INTERNAL_KEY_FOR_KEY_APK_URL} parameter")
            val keyVersion = inputData.getString(InternalConsts.INTERNAL_KEY_FOR_KEY_VERSION)
                ?: throw IllegalArgumentException("Input Data must have ${InternalConsts.INTERNAL_KEY_FOR_KEY_VERSION} parameter")
            val keyUpdateMessage = inputData.getString(InternalConsts.INTERNAL_KEY_FOR_KEY_UPDATE_MESSAGE)
                ?: throw IllegalArgumentException("Input Data must have ${InternalConsts.INTERNAL_KEY_FOR_KEY_UPDATE_MESSAGE} parameter")

            val response = HttpUtils.get(checkUrl) ?: return Result.failure()
            val parserParameters = ParserParameters(
                keyApkUrl = keyApkUrl,
                keyVersion = keyVersion,
                keyUpdateMessage = keyUpdateMessage
            )
            val checkModel = Parser.parseJson(response, parserParameters)
            Log.d(InternalConsts.LIBRARY_TAG, checkModel?.toString() ?: "no response")

            downloadApk(
                applicationContext,
                "https://raw.githubusercontent.com/feicien/android-auto-update/develop/extras/android-auto-update-v1.3.apk"
            )

            Result.success()
        } catch (e: Exception) {
            Log.e(InternalConsts.LIBRARY_TAG, e.toString())
            Result.failure()
        }
    }
}