package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.ui.model.AutoUpdateSettingsUiModel
import ru.glebik.updater.library.utils.AppVersionHelper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AutoUpdateDebugViewModel(
    private val appVersionHelper: AppVersionHelper,
    private val prefManager: AutoUpdateSharedPrefManager,
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
            is AutoUpdateSettingsIntent.ToggleWifi -> TODO()
            AutoUpdateSettingsIntent.CheckUpdate -> TODO()
            AutoUpdateSettingsIntent.DownloadAndInstallUpdate -> TODO()
        }
    }

    private fun initialLoad() {
        val appVersion = appVersionHelper.getAppVersion()
        val lastCheckTime = prefManager.lastCheckTimestamp
        val onlyWifi = prefManager.isWifiOnlyEnabled
        val isUpdateAvailable = prefManager.availableUpdate

        mutableState.update { state ->
            state.copy(
                model = AutoUpdateSettingsUiModel(
                    appVersion = appVersion,
                    isUpdateAvailable = isUpdateAvailable != null,
                    lastCheckTime = lastCheckTime?.let { formatTimestamp(it) },
                    onlyWifi = onlyWifi
                )
            )
        }
    }

    private fun checkUpdate() {

    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
}