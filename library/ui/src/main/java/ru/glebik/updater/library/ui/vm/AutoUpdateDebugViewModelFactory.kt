package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper

@Suppress("UNCHECKED_CAST")
class AutoUpdateDebugViewModelFactory(
    private val appVersionHelper: AppVersionHelper,
    private val prefManager: AutoUpdateSharedPrefManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutoUpdateDebugViewModel::class.java)) {
            return AutoUpdateDebugViewModel(appVersionHelper, prefManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
