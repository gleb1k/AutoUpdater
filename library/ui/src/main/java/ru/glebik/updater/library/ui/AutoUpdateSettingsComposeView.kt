package ru.glebik.updater.library.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AutoUpdate Settings", style = MaterialTheme.typography.headlineSmall)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📶 Установка только по Wi-Fi:")
            Switch(
                checked = model.onlyWifi,
                onCheckedChange = onToggleConnectionPreference
            )
        }

        Text("\uD83D\uDCE6 Версия приложения: ${model.appVersion}")
        Text("\uD83D\uDD52 Последняя проверка: ${model.lastCheckTime}")

        when (model.availableUpdate == null) {
            true -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCheckUpdateClick,
                    content = {
                        Text("Проверить наличие обновления")
                    }
                )
            }

            false -> {

                Text("Доступна новая версия!")

                Button(
                    onClick = downloadAndInstallUpdate,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Скачать и установить обновление")
                }
            }
        }

    }
}