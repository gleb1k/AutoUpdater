package ru.glebik.updater.library.parser

import org.json.JSONObject
import ru.glebik.updater.library.models.CheckModel

data class ParserParameters(
    val keyApkUrl: String,
    val keyVersion: String,
    val keyUpdateMessage: String?
)

object Parser {

    fun parseJson(jsonString: String, parserParameters: ParserParameters): CheckModel {
        val jsonObject = JSONObject(jsonString)
        val apkUrl = jsonObject.getString(parserParameters.keyApkUrl).trim()
        val version = jsonObject.getString(parserParameters.keyVersion).trim().toLong()
        val message = parserParameters.keyUpdateMessage?.let {
            jsonObject.optString(it).trim()
        } // null, если отсутствует
        return CheckModel(
            apkUrl = apkUrl,
            version = version,
            message = message

        )
    }
}