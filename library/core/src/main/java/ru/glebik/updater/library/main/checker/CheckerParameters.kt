package ru.glebik.updater.library.main.checker

import ru.glebik.updater.library.consts.DefaultConsts

//параметры которые указывают по каким ключам считывать нужные значения из json
data class CheckerParameters(
    val checkUrl: String,

    val keyApkUrl: String,
    val keyVersion: String,
    val keyUpdateMessage: String?,

    val needToDownload: Boolean = false
) {
    companion object {
        fun default(checkUrl: String, needToDownload: Boolean = true): CheckerParameters =
            CheckerParameters(
                checkUrl = checkUrl,
                keyApkUrl = DefaultConsts.KEY_APK_URL,
                keyVersion = DefaultConsts.KEY_VERSION,
                keyUpdateMessage = DefaultConsts.KEY_UPDATE_MESSAGE,
                needToDownload = needToDownload
            )
    }
}