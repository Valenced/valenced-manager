package app.valenced.manager.installer.step.download

import androidx.compose.runtime.Stable
import app.valenced.manager.R
import app.valenced.manager.installer.step.download.base.DownloadStep
import java.io.File

/**
 * Downloads the Valenced Xposed module
 *
 * https://github.com/Valenced/valenced
 */
@Stable
class DownloadModStep(
    workingDir: File
): DownloadStep() {

    override val nameRes = R.string.step_dl_mod

    override val downloadFullUrl: String = "https://github.com/Valenced/valenced/releases/latest/download/app-release.apk"
    override val destination = preferenceManager.moduleLocation
    override val workingCopy = workingDir.resolve("xposed.apk")

}
