package com.aliucord.manager.manager

import com.aliucord.manager.installers.Installer
import com.aliucord.manager.installers.pm.PMInstaller
import com.aliucord.manager.installers.root.RootInstaller
import com.aliucord.manager.installers.shizuku.ShizukuInstaller
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import rikka.shizuku.Shizuku
import kotlin.reflect.KClass

/**
 * Handle providing the correct install manager based on preferences.
 */
class InstallerManager(
    private val prefs: PreferencesManager,
) : KoinComponent {
    fun getActiveInstaller(): Installer =
        getInstaller(prefs.installer)

    fun getInstaller(type: InstallerSetting): Installer = when (type) {
        InstallerSetting.PM -> get<PMInstaller>()
        InstallerSetting.ROOT -> get<RootInstaller>()
        InstallerSetting.Shizuku -> get<ShizukuInstaller>()
    }

    fun getAvailableInstallers(): List<InstallerSetting> {
        val installers = mutableListOf(InstallerSetting.PM)
        try {
            if (Shizuku.pingBinder()) {
                installers.add(InstallerSetting.Shizuku)
            }
        } catch (e: Exception) {
            // Shizuku not available
        }
        try {
            val process = ProcessBuilder("su", "-c", "echo").start()
            process.waitFor()
            if (process.exitValue() == 0) {
                installers.add(InstallerSetting.ROOT)
            }
        } catch (e: Exception) {
            // Root not available
        }
        return installers
    }
}

enum class InstallerSetting(
    val installerClass: KClass<out Installer>,
) {
    PM(PMInstaller::class),
    ROOT(RootInstaller::class),
    Shizuku(ShizukuInstaller::class),
}

fun InstallerSetting.toDisplayName(): String = when (this) {
    InstallerSetting.PM -> "Package Manager"
    InstallerSetting.ROOT -> "Root"
    InstallerSetting.Shizuku -> "Shizuku"
}
