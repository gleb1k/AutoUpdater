package ru.glebik.updater.library.parser

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import ru.glebik.updater.library.consts.Consts
import ru.glebik.updater.library.models.CheckModel

object Parser {

    fun parseJson(jsonString: String): CheckModel? {
        return try {
            val jsonObject = JSONObject(jsonString)
            CheckModel(
                apkUrl = jsonObject.getString(Consts.KEY_APK_URL),
                version = jsonObject.getString(Consts.KEY_VERSION),
                message = jsonObject.optString(Consts.KEY_UPDATE_MESSAGE, null) // null, если отсутствует
            )
        } catch (e: JSONException) {
            Log.e(Consts.LIBRARY_TAG, "parse json error")
            null
        }
    }
}