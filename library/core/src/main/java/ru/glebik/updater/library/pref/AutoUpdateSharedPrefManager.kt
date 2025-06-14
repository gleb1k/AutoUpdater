package ru.glebik.updater.library.pref

import android.content.Context
import androidx.core.content.edit
import ru.glebik.updater.library.models.AvailableUpdate


interface AutoUpdateSharedPrefManager {
    var isWifiOnlyEnabled: Boolean
    var lastCheckTimestamp: Long?
    var availableUpdate: AvailableUpdate?

    fun clearAll()
}

class DefaultAutoUpdateSharedPrefManager(context: Context) : AutoUpdateSharedPrefManager {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var isWifiOnlyEnabled: Boolean
        get() = prefs.getBoolean(KEY_WIFI_ONLY, true)
        set(value) = prefs.edit { putBoolean(KEY_WIFI_ONLY, value) }

    override var lastCheckTimestamp: Long?
        get() = if (prefs.contains(KEY_LAST_CHECK)) prefs.getLong(KEY_LAST_CHECK, 0L) else null
        set(value) {
            if (value == null) {
                prefs.edit { remove(KEY_LAST_CHECK) }
            } else {
                prefs.edit { putLong(KEY_LAST_CHECK, value) }
            }
        }

    override var availableUpdate: AvailableUpdate?
        get() {
            val raw = prefs.getString(KEY_AVAILABLE_UPDATE, null) ?: return null
            val parts = raw.split("||")
            if (parts.size < 2) return null
            val apkUrl = parts[0]
            val version = parts[1].toLongOrNull() ?: return null
            val message = if (parts.size >= 3) parts[2] else null
            return AvailableUpdate(apkUrl, version, message)
        }
        set(value) {
            if (value == null) {
                prefs.edit { remove(KEY_AVAILABLE_UPDATE) }
            } else {
                val serialized = buildString {
                    append(value.apkUrl)
                    append("||")
                    append(value.version)
                    append("||")
                    append(value.message.orEmpty())
                }
                prefs.edit { putString(KEY_AVAILABLE_UPDATE, serialized) }
            }
        }

    override fun clearAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "auto_updater_prefs"
        private const val KEY_WIFI_ONLY = "key_wifi_only"
        private const val KEY_AVAILABLE_UPDATE = "available_update"
        private const val KEY_LAST_CHECK = "key_last_check"
    }
}