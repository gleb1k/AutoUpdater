package ru.glebik.updater.library.ui.vm

sealed interface AutoUpdateSettingsIntent {
    data object Init : AutoUpdateSettingsIntent
    data class ToggleWifi(val downloadWithWifiEnable: Boolean) : AutoUpdateSettingsIntent
    data object CheckUpdate : AutoUpdateSettingsIntent
    data object DownloadAndInstallUpdate : AutoUpdateSettingsIntent
}