package ru.glebik.updater.library.ui.vm

sealed interface AutoUpdateSettingsIntent {
    data object Init : AutoUpdateSettingsIntent
}