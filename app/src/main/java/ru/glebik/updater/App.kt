package ru.glebik.updater

import android.app.Application
import ru.glebik.updater.library.AutoUpdater


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AutoUpdater.init(this)
    }
}