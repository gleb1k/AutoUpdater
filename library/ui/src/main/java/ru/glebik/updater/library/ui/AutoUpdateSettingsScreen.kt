package ru.glebik.updater.library.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.glebik.updater.library.ui.vm.AutoUpdateDebugViewModel

@Composable
fun AutoUpdateSettingsScreen(
    viewModel: AutoUpdateDebugViewModel
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    AutoUpdateDebugComposeView(
        modifier = Modifier,
        model = state.model,
        onCheckUpdateClick = {},
        onToggleConnectionPreference = {}
    )
}