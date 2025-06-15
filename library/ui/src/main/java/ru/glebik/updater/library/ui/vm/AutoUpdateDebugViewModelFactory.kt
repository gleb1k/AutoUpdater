package ru.glebik.updater.library.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import ru.glebik.updater.library.main.checker.CheckerParameters
import ru.glebik.updater.library.pref.AutoUpdateSharedPrefManager
import ru.glebik.updater.library.utils.AppVersionHelper
import ru.glebik.updater.library.utils.NetworkChecker

@Suppress("UNCHECKED_CAST")
class AutoUpdateDebugViewModelFactory(
    private val networkChecker: NetworkChecker,
    private val checkerWorkManager: WorkManager,
    private val checkerParameters: CheckerParameters,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AutoUpdateDebugViewModel::class.java)) {
            return AutoUpdateDebugViewModel(networkChecker, checkerWorkManager, checkerParameters) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
