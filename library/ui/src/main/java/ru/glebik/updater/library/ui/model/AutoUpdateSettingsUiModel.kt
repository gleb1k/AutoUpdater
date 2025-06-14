package ru.glebik.updater.library.ui.model

data class AutoUpdateSettingsUiModel(
    val appVersion: String,
    val lastCheckTime: String?,
    val isUpdateAvailable: Boolean,
    val onlyWifi: Boolean,
)