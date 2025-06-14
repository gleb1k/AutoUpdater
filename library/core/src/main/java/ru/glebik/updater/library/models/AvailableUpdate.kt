package ru.glebik.updater.library.models

data class AvailableUpdate(
    val apkUrl: String,
    val version: Long,
    val message: String?,
)