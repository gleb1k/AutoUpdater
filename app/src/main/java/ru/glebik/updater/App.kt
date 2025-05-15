package ru.glebik.updater

import android.app.Application
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.main.checker.CheckerParameters
import ru.glebik.updater.library.consts.InternalConsts.CHECK_URL_EXAMPLE
import ru.glebik.updater.library.init.UpdateConfig
import java.util.concurrent.TimeUnit


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AutoUpdater.init(this)
    }
}