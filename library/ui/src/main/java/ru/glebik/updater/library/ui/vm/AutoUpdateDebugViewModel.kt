package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.init.UpdateConfig
import ru.glebik.updater.library.main.checker.CheckerParameters
import ru.glebik.updater.library.ui.model.AutoUpdateSettingsUiModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AutoUpdateDebugViewModel(
    private val workManager: WorkManager,
    private val checkerParameters: CheckerParameters,
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
            is AutoUpdateSettingsIntent.ToggleWifi -> toggleWifi(intent.downloadWithWifiEnable)
            AutoUpdateSettingsIntent.CheckUpdate -> checkUpdate()
            AutoUpdateSettingsIntent.DownloadAndInstallUpdate -> downloadAndInstallUpdate()
        }
    }

    private fun initialLoad() {
        val appVersion = AutoUpdater.appVersionHelper.getAppVersion()
        val lastCheckTime = AutoUpdater.prefManager.lastCheckTimestamp
        val onlyWifi = AutoUpdater.prefManager.isWifiOnlyEnabled
        val update = AutoUpdater.prefManager.availableUpdate

        mutableState.update { state ->
            state.copy(
                model = AutoUpdateSettingsUiModel(
                    appVersion = appVersion,
                    availableUpdate = update,
                    lastCheckTime = lastCheckTime?.let { formatTimestamp(it) } ?: "-",
                    onlyWifi = onlyWifi
                )
            )
        }
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            val requestId = AutoUpdater.checkUpdate(UpdateConfig(checkerParameters, false))
            workManager.getWorkInfoByIdFlow(requestId)
                .filter { it.state.isFinished }
                .collect { info ->
                    if (info.state == WorkInfo.State.SUCCEEDED) {
                        val update = AutoUpdater.prefManager.availableUpdate
                        val lastCheckTime = AutoUpdater.prefManager.lastCheckTimestamp
                        mutableState.update { state ->
                            state.copy(
                                model = state.model.copy(
                                    availableUpdate = update,
                                    lastCheckTime = lastCheckTime?.let { formatTimestamp(it) },
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun downloadAndInstallUpdate() {
        AutoUpdater.downloadAndInstallAvailableUpdate()
    }

    private fun toggleWifi(enable: Boolean) {

    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
}