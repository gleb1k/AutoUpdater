package ru.glebik.updater.library.ui.model

data class AutoUpdateSettingsUiModel(
    val appVersion: String,
    val isUpdateAvailable: Boolean,
    val lastCheckTime: String,
    val lastDownloadTime: String,
    val onlyWifi: Boolean,
)