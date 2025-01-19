package ru.glebik.updater.library.parser

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import ru.glebik.updater.library.consts.InternalConsts
import ru.glebik.updater.library.models.CheckModel

data class ParserParameters(
    val keyApkUrl: String,
    val keyVersion: String,
    val keyUpdateMessage: String?
)

object Parser {

    fun parseJson(jsonString: String, parserParameters: ParserParameters): CheckModel? {
        return try {
            val jsonObject = JSONObject(jsonString)
            val apkUrl = jsonObject.getString(parserParameters.keyApkUrl).trim()
            val version = jsonObject.getString(parserParameters.keyVersion).trim()
            val message = parserParameters.keyUpdateMessage?.let {
                jsonObject.optString(it).trim()
            } // null, если отсутствует
            CheckModel(
                apkUrl = apkUrl,
                version = version,
                message = message

            )
        } catch (e: JSONException) {
            Log.e(InternalConsts.LIBRARY_TAG, "parse json error")
            null
        }
    }
}