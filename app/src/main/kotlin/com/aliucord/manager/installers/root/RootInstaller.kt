package com.aliucord.manager.installers.root

import com.aliucord.manager.installers.Installer
import com.aliucord.manager.installers.InstallerResult
import com.aliucord.manager.installers.InstallerResult.Error
import java.io.File
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.coroutines.runBlocking

class RootInstaller : Installer {
    override suspend fun install(apks: List<File>, silent: Boolean) {
        waitInstall(apks, silent)
    }

    override suspend fun waitInstall(apks: List<File>, silent: Boolean): InstallerResult {
        // Create install session
        val installCommand = StringBuilder("pm install-create")
        if (silent) {
            installCommand.append(" -r --user 0")
        }
        val output = execute(installCommand.toString())
        val sessionId = output.substringAfter("Success: created install session [").substringBefore("]").toIntOrNull()
        if (sessionId == null) {
            println("RootInstaller: install-create failed: $output")
            return PMInstallerError("Failed to create install session: $output")
        }

        // Write each APK to the session
        for (apk in apks) {
            val writeCmd = "pm install-write -S ${apk.length()} $sessionId ${apk.name} - < \"${apk.absolutePath}\""
            val writeOutput = execute(writeCmd)
            if (!writeOutput.contains("Success")) {
                println("RootInstaller: install-write failed: $writeOutput")
                execute("pm install-abandon $sessionId")
                return PMInstallerError("Failed to write APK: $writeOutput")
            }
        }

        // Commit the session
        val commitOutput = execute("pm install-commit $sessionId")
        if (!commitOutput.contains("Success")) {
            println("RootInstaller: install-commit failed: $commitOutput")
            return PMInstallerError("Failed to commit install session: $commitOutput")
        }

        return InstallerResult.Success
    }

    override suspend fun waitUninstall(packageName: String): InstallerResult {
        val output = execute("pm uninstall $packageName")
        if (!output.contains("Success")) {
            println("RootInstaller: uninstall failed: $output")
            return PMInstallerError("Failed to uninstall: $output")
        }
        return InstallerResult.Success
    }

    private fun execute(command: String): String {
        return try {
            val process = ProcessBuilder("su", "-c", command).start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output
        } catch (e: Exception) {
            println("RootInstaller: Exception during command execution: ${e.message}")
            e.toString()
        }
    }
}

@Parcelize
data class PMInstallerError(private val reason: String) : Error(), Parcelable {
    override fun getDebugReason(): String = reason
}
