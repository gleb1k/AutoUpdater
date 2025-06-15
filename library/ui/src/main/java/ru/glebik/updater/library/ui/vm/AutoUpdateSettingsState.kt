package ru.glebik.updater.library.ui.vm

import ru.glebik.updater.library.ui.model.AutoUpdateSettingsUiModel

data class AutoUpdateSettingsState(
    val model: AutoUpdateSettingsUiModel,
) {
    companion object {
        val EMPTY = AutoUpdateSettingsState(
            AutoUpdateSettingsUiModel(
                appVersion = "-",
                availableUpdate = null,
                lastCheckTime = null,
                onlyWifi = false,
                isLoading = false
            )
        )
    }
}