package ru.glebik.updater

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import ru.glebik.updater.consts.getSampleOneTimeCheckerParameters
import ru.glebik.updater.library.init.UpdateConfig
import ru.glebik.updater.library.ui.AutoUpdateSettingsScreen
import ru.glebik.updater.library.ui.vm.AutoUpdateDebugViewModelFactory
import ru.glebik.updater.ui.theme.AutoUpdaterTheme


class MainActivity : ComponentActivity() {
    private val installPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Разрешение предоставлено
            } else {
                // Разрешение не предоставлено
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка разрешения на установку приложений из неизвестных источников
        if (!this.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${applicationContext.packageName}")
            }
            // Используем ActivityResultLauncher вместо startActivityForResult, который устарел
            installPermissionLauncher.launch(intent)
        }
//
//// Проверка разрешений на доступ к хранилищу (для Android 6.0 и выше)
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED ||
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ),
//                1
//            )
//        }

        enableEdgeToEdge()
        setContent {
            AutoUpdaterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val factory = remember {
                            AutoUpdateDebugViewModelFactory(
                                WorkManager.getInstance(applicationContext),
                                getSampleOneTimeCheckerParameters(),
                            )
                        }
                        AutoUpdateSettingsScreen(viewModel = viewModel(factory = factory))
                    }
                }
            }
        }
    }
}