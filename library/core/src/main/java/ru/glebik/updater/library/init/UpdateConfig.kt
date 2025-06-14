package ru.glebik.updater.library.init

import ru.glebik.updater.library.main.checker.CheckerParameters


data class UpdateConfig(
    val checkerParameters: CheckerParameters,
    val needToDownloadAfterCheck: Boolean,
)