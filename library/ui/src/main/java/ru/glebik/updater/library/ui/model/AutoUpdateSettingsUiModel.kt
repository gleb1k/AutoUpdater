package ru.glebik.updater.library.ui.model

import ru.glebik.updater.library.models.AvailableUpdate

data class AutoUpdateSettingsUiModel(
    val appVersion: String,
    val lastCheckTime: String?,
    val availableUpdate: AvailableUpdate?,
    val onlyWifi: Boolean,
    val isLoading: Boolean,
)