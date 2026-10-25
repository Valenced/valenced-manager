package app.valenced.manager.installer.step.download

import androidx.compose.runtime.Stable
import app.valenced.manager.R
import app.valenced.manager.domain.manager.BundleManager
import app.valenced.manager.installer.step.download.base.DownloadStep
import org.koin.core.component.inject
import java.io.File

/**
 * Downloads the Valenced Xposed module from the release the patch bundle is from
 *
 * https://github.com/Valenced/valenced
 */
@Stable
class DownloadModStep(
    workingDir: File
): DownloadStep() {

    private val bundleManager: BundleManager by inject()

    override val nameRes = R.string.step_dl_mod

    override val downloadFullUrl: String
        get() = bundleManager.bundle?.moduleUrl ?: error("The patch bundle isn't loaded")
    override val destination = preferenceManager.moduleLocation
    override val workingCopy = workingDir.resolve("xposed.apk")

}
