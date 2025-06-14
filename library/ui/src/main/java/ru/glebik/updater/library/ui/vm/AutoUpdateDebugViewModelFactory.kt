package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import ru.glebik.updater.library.main.checker.CheckerParameters
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper

@Suppress("UNCHECKED_CAST")
class AutoUpdateDebugViewModelFactory(
    private val checkerWorkManager: WorkManager,
    private val checkerParameters: CheckerParameters,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutoUpdateDebugViewModel::class.java)) {
            return AutoUpdateDebugViewModel(checkerWorkManager, checkerParameters) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
