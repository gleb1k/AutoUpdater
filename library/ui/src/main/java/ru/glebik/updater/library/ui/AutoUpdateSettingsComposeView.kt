package ru.glebik.updater.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.glebik.updater.library.ui.model.AutoUpdateSettingsUiModel

@Composable
fun AutoUpdateDebugComposeView(
    modifier: Modifier = Modifier,
    model: AutoUpdateSettingsUiModel,
    onCheckUpdateClick: () -> Unit,
    downloadAndInstallUpdate: () -> Unit,
    onToggleConnectionPreference: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.auto_update_settings_title), style = MaterialTheme.typography.headlineSmall)

        Text(stringResource(R.string.section_network), style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.only_wifi))
            Switch(
                checked = model.onlyWifi,
                onCheckedChange = onToggleConnectionPreference
            )
        }

        Text(stringResource(R.string.section_info), style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        Text(stringResource(R.string.app_version, model.appVersion))
        Text(
            stringResource(
                R.string.last_check,
                model.lastCheckTime ?: "-"
            )
        )

        Text(stringResource(R.string.section_update), style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()

        if (model.availableUpdate == null) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCheckUpdateClick
            ) {
                Text(stringResource(R.string.check_for_update))
            }
        } else {
            Text(stringResource(R.string.new_version_available))
            Button(
                onClick = downloadAndInstallUpdate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.download_and_install))
            }
        }
    }
}