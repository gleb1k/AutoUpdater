package ru.glebik.updater.library.ui.vm

sealed interface AutoUpdateSettingsEffect {
    data class ShowToast(val stringRes: Int) : AutoUpdateSettingsEffect
}