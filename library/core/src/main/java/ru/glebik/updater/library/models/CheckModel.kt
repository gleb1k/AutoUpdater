package ru.glebik.updater.library.models

data class CheckModel(
    val apkUrl: String,
    val version: Long,
    val message: String?,
)