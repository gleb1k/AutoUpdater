package ru.glebik.updater.library.ui

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.glebik.updater.library.ui.vm.AutoUpdateDebugViewModel
import ru.glebik.updater.library.ui.vm.AutoUpdateSettingsEffect
import ru.glebik.updater.library.ui.vm.AutoUpdateSettingsIntent

@Composable
fun AutoUpdateSettingsScreen(
    viewModel: AutoUpdateDebugViewModel
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            when (it) {
                is AutoUpdateSettingsEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(context.getString(it.stringRes))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        AutoUpdateDebugComposeView(
            modifier = Modifier.padding(it),
            model = state.model,
            onCheckUpdateClick = {
                viewModel.handleIntent(AutoUpdateSettingsIntent.CheckUpdate)
            },
            onToggleConnectionPreference = {
                viewModel.handleIntent(AutoUpdateSettingsIntent.ToggleWifi(it))
            },
            downloadAndInstallUpdate = {
                viewModel.handleIntent(AutoUpdateSettingsIntent.DownloadAndInstallUpdate)
            }
        )
    }
}