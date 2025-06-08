package ru.glebik.updater.library.models.mapper

import androidx.work.Data
import ru.glebik.updater.library.consts.InternalConsts
import ru.glebik.updater.library.main.checker.CheckerParameters

object CheckerParamsMapper {
    fun map(params: CheckerParameters): Data = Data.Builder()
        .putString(InternalConsts.INTERNAL_KEY_FOR_CHECK_URL, params.checkUrl)
        .putString(InternalConsts.INTERNAL_KEY_FOR_KEY_APK_URL, params.keyApkUrl)
        .putString(InternalConsts.INTERNAL_KEY_FOR_KEY_VERSION, params.keyVersion)
        .putString(InternalConsts.INTERNAL_KEY_FOR_KEY_UPDATE_MESSAGE, params.keyUpdateMessage)
        .putBoolean(InternalConsts.INTERNAL_KEY_FOR_NEED_TO_DOWNLOAD, params.needToDownload)
        .build()
}