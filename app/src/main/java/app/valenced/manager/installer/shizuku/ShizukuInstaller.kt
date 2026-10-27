package app.valenced.manager.installer.shizuku

import android.content.Context
import app.valenced.manager.R
import app.valenced.manager.domain.manager.InstallManager
import app.valenced.manager.installer.Installer
import app.valenced.manager.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rikka.shizuku.Shizuku
import java.io.File

class ShizukuInstaller(private val context: Context) : Installer, KoinComponent {

    val installManager: InstallManager by inject()

    override suspend fun installApks(silent: Boolean, vararg apks: File) {
        if (!ShizukuPermissions.waitShizukuPermissions()) {
            withContext(Dispatchers.Main) {
                context.showToast(R.string.msg_shizuku_denied, short = false)
            }

            throw Error("Failed to install due to missing Shizuku permissions")
        }

        // The shell user can't read the app's internal storage, so the splits are streamed to pm
        val createResult = executeShellCommand("pm install-create -S ${apks.sumOf { it.length() }}")
        val sessionId = Regex("""\[(\d+)]""").find(createResult)?.groupValues?.get(1)
            ?: throw RuntimeException("Failed to create install session: $createResult")

        apks.forEach {
            executeShellCommand("pm install-write -S ${it.length()} $sessionId ${it.name} -", input = it)
        }

        val commitResult = executeShellCommand("pm install-commit $sessionId")
        if (!commitResult.startsWith("Success")) throw RuntimeException("Failed to install: $commitResult")

        installManager.getInstalled()
    }

    private fun executeShellCommand(command: String, input: File? = null): String {
        @Suppress("DEPRECATION")
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)

        process.outputStream.use { stdin -> input?.inputStream()?.use { it.copyTo(stdin) } }

        val errorStr = process.errorStream.bufferedReader().use { it.readText().trim() }
        if(errorStr.isNotBlank()) throw RuntimeException("Failed to execute $command:\n\n$errorStr")

        return process.inputStream.bufferedReader().use { it.readText().trim() }
    }

}
