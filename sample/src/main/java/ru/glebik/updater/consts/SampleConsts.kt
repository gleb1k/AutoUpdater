package ru.glebik.updater.consts

import ru.glebik.updater.library.consts.DefaultConsts
import ru.glebik.updater.library.main.checker.CheckerParameters


private const val CHECK_URL_EXAMPLE =
    "https://raw.githubusercontent.com/gleb1k/AutoUpdater/refs/heads/master/extras/update.json"

fun getSampleOneTimeCheckerParameters() : CheckerParameters.OneTime = CheckerParameters.OneTime(
    checkUrl = CHECK_URL_EXAMPLE,
    keyApkUrl = DefaultConsts.KEY_APK_URL,
    keyVersion = DefaultConsts.KEY_VERSION,
    keyUpdateMessage = DefaultConsts.KEY_UPDATE_MESSAGE,
)