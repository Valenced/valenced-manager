package app.valenced.manager.updatechecker.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.valenced.manager.domain.manager.BundleManager
import app.valenced.manager.domain.manager.InstallManager
import app.valenced.manager.updatechecker.reciever.UpdateBroadcastReceiver
import app.valenced.manager.utils.DiscordVersion
import app.valenced.manager.utils.Intents
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    val bundleManager: BundleManager by inject()
    val installManager: InstallManager by inject()

    override suspend fun doWork(): Result {
        val currentVersion =
            DiscordVersion.fromVersionCode(installManager.current?.versionCode.toString())
                ?: return Result.success()
        val targetVersion = runCatching { bundleManager.load(refresh = true) }.getOrNull()?.discordVersion
            ?: return Result.retry()

        if (targetVersion > currentVersion) {
            context.sendBroadcast(
                Intent(
                    context,
                    UpdateBroadcastReceiver::class.java
                ).apply {
                    putExtra(Intents.Extras.VERSION, targetVersion.toVersionCode())
                })
        }

        return Result.success()
    }

}
