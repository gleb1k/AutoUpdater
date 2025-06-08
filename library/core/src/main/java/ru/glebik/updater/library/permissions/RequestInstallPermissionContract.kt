package ru.glebik.updater.library.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri

//class RequestInstallPermissionContract : ActivityResultContract<Context, Boolean>() {
//    override fun createIntent(context: Context, input: Context): Intent {
//        return Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
//            data = ("package:" + input.packageName).toUri()
//        }
//    }
//
//    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
//        // Проверяем текущее разрешение
//        return intent?.component?.packageName?.packageManager?.canRequestPackageInstalls() ?: false
//    }
//}