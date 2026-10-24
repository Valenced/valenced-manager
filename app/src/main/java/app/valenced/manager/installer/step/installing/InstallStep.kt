package app.valenced.manager.installer.step.installing

import android.content.Context
import app.valenced.manager.R
import app.valenced.manager.domain.manager.InstallMethod
import app.valenced.manager.domain.manager.PreferenceManager
import app.valenced.manager.installer.Installer
import app.valenced.manager.installer.session.SessionInstaller
import app.valenced.manager.installer.shizuku.ShizukuInstaller
import app.valenced.manager.installer.shizuku.ShizukuPermissions
import app.valenced.manager.installer.step.Step
import app.valenced.manager.installer.step.StepGroup
import app.valenced.manager.installer.step.StepRunner
import app.valenced.manager.utils.isMiui
import app.valenced.manager.utils.showToast
import org.koin.core.component.inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Installs all the modified splits with the users desired [Installer]
 *
 * @see SessionInstaller
 * @see ShizukuInstaller
 *
 * @param apksDir Where all the patched APKs are
 */
class InstallStep(
    private val apksDir: File
): Step() {

    private val preferences: PreferenceManager by inject()
    private val context: Context by inject()

    override val group = StepGroup.INSTALLING
    override val nameRes = R.string.step_installing

    override suspend fun run(runner: StepRunner) {
        runner.logger.i("Installing apks")
        val files = apksDir.listFiles()
            ?.takeIf { it.isNotEmpty() }
            ?: throw Error("Missing APKs from patching step; failure likely")

        val installMethod = if (preferences.installMethod == InstallMethod.SHIZUKU && !ShizukuPermissions.waitShizukuPermissions()) {
            // Temporarily use DEFAULT if SHIZUKU permissions are not granted
            withContext(Dispatchers.Main) {
                context.applicationContext.showToast(R.string.msg_shizuku_denied)
            }
            InstallMethod.DEFAULT
        } else {
            preferences.installMethod
        }

        val installer: Installer = when (installMethod) {
            InstallMethod.DEFAULT -> SessionInstaller(context)
            InstallMethod.SHIZUKU -> ShizukuInstaller(context)
        }

        installer.installApks(silent = !isMiui, *files)
    }

}