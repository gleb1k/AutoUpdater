package ru.glebik.updater.library.utils

import android.content.Context
import android.content.pm.PackageManager

object AppUtils {

    // Получение названия приложения
    fun getAppName(context: Context): String {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            context.packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "UnknownAppName"
        }
    }

    fun getAppApkFileName(context: Context): String {
        val appNameFileName = getAppName(context)
            .replace("\\s+".toRegex(), "-") // Удаляем пробелы
            .lowercase() // Преобразуем в строчные буквы
        return "$appNameFileName.apk"
    }

}