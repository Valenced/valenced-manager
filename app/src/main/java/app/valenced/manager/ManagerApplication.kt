package app.valenced.manager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.reseam.sdk.ReseamAndroidHost
import app.valenced.manager.di.httpModule
import app.valenced.manager.di.managerModule
import app.valenced.manager.di.repositoryModule
import app.valenced.manager.di.viewModelModule
import app.valenced.manager.domain.manager.PreferenceManager
import app.valenced.manager.domain.manager.UpdateCheckerDuration
import app.valenced.manager.updatechecker.worker.UpdateWorker
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ManagerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ReseamAndroidHost.setClassLoader(classLoader)
        initNotificationChannels()

        startKoin {
            androidContext(this@ManagerApplication)
            modules(
                httpModule,
                managerModule,
                viewModelModule,
                repositoryModule
            )
        }

        val prefs: PreferenceManager = get()

        if (prefs.updateDuration != UpdateCheckerDuration.DISABLED) {
            val duration = prefs.updateDuration
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "app.valenced.manager.UPDATE_CHECK",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UpdateWorker>(duration.time, duration.unit).build()
            )
        }
    }

    private fun initNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val updates = NotificationChannel(
            "${BuildConfig.APPLICATION_ID}.notifications.UPDATE",
            "Discord updates",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        nm.createNotificationChannel(updates)
    }

}