package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.main.checker.CheckerParameters
import ru.glebik.updater.library.ui.R
import ru.glebik.updater.library.ui.model.AutoUpdateSettingsUiModel
import ru.glebik.updater.library.utils.NetworkChecker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AutoUpdateDebugViewModel(
    private val networkChecker: NetworkChecker,
    private val workManager: WorkManager,
    private val checkerParameters: CheckerParameters,
) : ViewModel() {

    private val mutableState by lazy { MutableStateFlow(AutoUpdateSettingsState.EMPTY) }
    val state: StateFlow<AutoUpdateSettingsState>
        get() = mutableState.asStateFlow()

    private val mutableEffect by lazy { MutableSharedFlow<AutoUpdateSettingsEffect>() }
    val effect: SharedFlow<AutoUpdateSettingsEffect>
        get() = mutableEffect.asSharedFlow()

    init {
        handleIntent(AutoUpdateSettingsIntent.Init)
    }

    fun handleIntent(intent: AutoUpdateSettingsIntent) {
        when (intent) {
            AutoUpdateSettingsIntent.Init -> initialLoad()
            is AutoUpdateSettingsIntent.ToggleWifi -> toggleWifi(intent.downloadWithWifiEnable)
            AutoUpdateSettingsIntent.CheckUpdate -> checkUpdate()
            AutoUpdateSettingsIntent.DownloadAndInstallUpdate -> downloadAndInstallUpdateIfAllowed()
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
                    onlyWifi = onlyWifi,
                    isLoading = false
                )
            )
        }
    }

    private fun checkUpdate() {
        viewModelScope.launch {
            mutableState.update { state -> state.copy(model = state.model.copy(isLoading = true)) }

            val requestId = AutoUpdater.checkUpdate(checkerParameters)
            workManager.getWorkInfoByIdFlow(requestId)
                .filter { it.state.isFinished }
                .collect { info ->
                    if (info.state == WorkInfo.State.SUCCEEDED) {
                        val update = AutoUpdater.prefManager.availableUpdate
                        val lastCheckTime = AutoUpdater.prefManager.lastCheckTimestamp

                        val stringRes = if (update != null) {
                            R.string.update_found
                        } else {
                            R.string.update_not_found
                        }

                        mutableEffect.emit(AutoUpdateSettingsEffect.ShowToast(stringRes))

                        mutableState.update { state ->
                            state.copy(
                                model = state.model.copy(
                                    availableUpdate = update,
                                    lastCheckTime = lastCheckTime?.let { formatTimestamp(it) },
                                    isLoading = false
                                )
                            )
                        }
                    }
                    if (info.state == WorkInfo.State.FAILED || info.state == WorkInfo.State.CANCELLED) {
                        mutableEffect.emit(AutoUpdateSettingsEffect.ShowToast(R.string.update_check_error))
                    }
                }
        }
    }

    private fun downloadAndInstallUpdateIfAllowed() {
        viewModelScope.launch {
            if (AutoUpdater.prefManager.isWifiOnlyEnabled && networkChecker.isWifiConnected()
                    .not()
            ) {
                mutableEffect.emit(AutoUpdateSettingsEffect.ShowToast(R.string.error_wifi_required))
                return@launch
            }
            mutableState.update { state -> state.copy(model = state.model.copy(isLoading = true)) }
            AutoUpdater.downloadAndInstallAvailableUpdate()
        }
    }

    private fun toggleWifi(enable: Boolean) {
        AutoUpdater.prefManager.isWifiOnlyEnabled = enable

        mutableState.update { state ->
            state.copy(
                model = state.model.copy(onlyWifi = enable)
            )
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
}