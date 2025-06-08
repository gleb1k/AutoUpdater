package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.ui.model.AutoUpdateSettingsUiModel

class AutoUpdateDebugViewModel(

) : ViewModel() {

    private val mutableState by lazy { MutableStateFlow(AutoUpdateSettingsState.EMPTY) }
    val state: StateFlow<AutoUpdateSettingsState>
        get() = mutableState.asStateFlow()

    init {
        handleIntent(AutoUpdateSettingsIntent.Init)
    }

    fun handleIntent(intent: AutoUpdateSettingsIntent) {
        when (intent) {
            AutoUpdateSettingsIntent.Init -> initialLoad()
        }
    }

    private fun initialLoad() {
        val appVersion = AutoUpdater.appVersionHelper.getAppVersion()
        val lastCheckTime = "08.06.2025"
        val lastDownloadTime = "07.06.2025"
        val onlyWifi = false
        val isUpdateAvailable = false

        mutableState.update {
            it.copy(
                model = AutoUpdateSettingsUiModel(
                    appVersion = appVersion,
                    isUpdateAvailable = isUpdateAvailable,
                    lastCheckTime = lastCheckTime,
                    lastDownloadTime = lastDownloadTime,
                    onlyWifi = onlyWifi
                )
            )
        }
    }
}