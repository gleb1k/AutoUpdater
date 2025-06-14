package ru.glebik.updater.library.main.checker

import ru.glebik.updater.library.consts.DefaultConsts
import java.util.concurrent.TimeUnit

//параметры которые указывают по каким ключам считывать нужные значения из json
sealed interface CheckerParameters {
    val checkUrl: String
    val keyApkUrl: String
    val keyVersion: String
    val keyUpdateMessage: String?

    data class OneTime(
        override val checkUrl: String,
        override val keyApkUrl: String,
        override val keyVersion: String,
        override val keyUpdateMessage: String?
    ) : CheckerParameters

    data class Periodic(
        override val checkUrl: String,
        override val keyApkUrl: String,
        override val keyVersion: String,
        override val keyUpdateMessage: String?,

        val isPeriodic: Boolean,
        val repeatInterval: Long,
        val timeUnit: TimeUnit
    ) : CheckerParameters

}