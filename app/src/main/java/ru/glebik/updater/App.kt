package ru.glebik.updater

import android.app.Application
import ru.glebik.updater.library.AutoUpdater
import ru.glebik.updater.library.checker.CheckerParameters
import ru.glebik.updater.library.consts.InternalConsts.CHECK_URL_EXAMPLE
import ru.glebik.updater.library.init.UpdateConfig
import java.util.concurrent.TimeUnit


class App : Application() {

    override fun onCreate() {
        super.onCreate()

//        val checkerParameters = CheckerParameters.default(CHECK_URL_EXAMPLE)
//
//        val updateConfig = UpdateConfig.Builder.builder()
//            .setCheckerParameters(checkerParameters)
//            .setPeriodic() // Choose periodic mode
//            .setInterval(6, TimeUnit.HOURS) // Set interval
//            .build()
//
//        AutoUpdater.start(this, updateConfig)
    }
}