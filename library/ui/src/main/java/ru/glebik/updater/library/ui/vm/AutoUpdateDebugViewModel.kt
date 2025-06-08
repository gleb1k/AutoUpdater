package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    }
}