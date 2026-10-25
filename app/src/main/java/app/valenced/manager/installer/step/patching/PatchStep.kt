package app.valenced.manager.installer.step.patching

import app.reseam.sdk.LogLevel
import app.reseam.sdk.OptionValue
import app.reseam.sdk.PatchOutput
import app.reseam.sdk.PatchRequest
import app.reseam.sdk.PatchSelection
import app.reseam.sdk.PatchStatus
import app.reseam.sdk.RunEvent
import app.reseam.sdk.Trust
import app.reseam.sdk.patch
import app.valenced.manager.BuildConfig
import app.valenced.manager.R
import app.valenced.manager.domain.manager.BundleManager
import app.valenced.manager.domain.manager.PreferenceManager
import app.valenced.manager.installer.step.Step
import app.valenced.manager.installer.step.StepGroup
import app.valenced.manager.installer.step.StepRunner
import app.valenced.manager.installer.step.download.DownloadBaseStep
import app.valenced.manager.installer.step.download.DownloadLangStep
import app.valenced.manager.installer.step.download.DownloadLibsStep
import app.valenced.manager.installer.step.download.DownloadModStep
import app.valenced.manager.installer.step.download.DownloadResourcesStep
import app.valenced.manager.installer.util.Logger
import app.valenced.manager.installer.util.Signer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.io.File

/**
 * Patches Discord with the Valenced bundle using Reseam, then signs it
 *
 * @param outputDir Where the patched and signed apks are written
 */
class PatchStep(
    private val outputDir: File
) : Step() {

    private val preferences: PreferenceManager by inject()
    private val bundleManager: BundleManager by inject()

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_patch

    override suspend fun run(runner: StepRunner) {
        val baseApk = runner.getCompletedStep<DownloadBaseStep>().workingCopy
        val libsApk = runner.getCompletedStep<DownloadLibsStep>().workingCopy
        val langApk = runner.getCompletedStep<DownloadLangStep>().workingCopy
        val resApk = runner.getCompletedStep<DownloadResourcesStep>().workingCopy
        val module = runner.getCompletedStep<DownloadModStep>().workingCopy

        val bundle = bundleManager.load()
        if (bundle.custom) runner.logger.w("Using custom patch bundle ${bundle.file}, signed by ${bundle.signer}")

        val request = PatchRequest(
            apkPath = baseApk.absolutePath,
            splitPaths = listOf(libsApk, langApk, resApk).map { it.absolutePath },
            bundlePaths = listOf(bundle.file.absolutePath),
            trust = Trust(keys = listOf(bundle.signer)),
            selection = selection(module),
            output = PatchOutput.SplitDir(outputDir.absolutePath),
            signing = Signer.keyFiles,
        )

        val outcome = withContext(Dispatchers.IO) {
            patch(request) { event -> runner.logger.log(event) }
        }
        val metrics = outcome.metrics
        runner.logger.i(
            "Patched in ${metrics.totalDurationMs} ms" +
                (metrics.peakRssBytes?.let { ", peak memory ${it / 1_048_576u} MB" } ?: "")
        )
    }

    private fun selection(module: File) = PatchSelection(
        enable = listOfNotNull(DEBUGGABLE.takeIf { preferences.debuggable }),
        disable = listOfNotNull(ICON.takeUnless { preferences.patchIcon }),
        options = mapOf(
            VALENCED to mapOf("module" to OptionValue.Path(module.absolutePath)),
            PACKAGE_NAME to mapOf(
                "packageName" to OptionValue.Text(preferences.packageName.ifBlank { BuildConfig.MODDED_APP_PACKAGE_NAME })
            ),
            APP_NAME to mapOf("appName" to OptionValue.Text(preferences.appName)),
        ),
    )

    private fun Logger.log(event: RunEvent) = when (event) {
        is RunEvent.Info -> i(event.message)
        is RunEvent.PatchStarted -> d("Applying ${event.patch}")
        is RunEvent.PatchLog -> when (event.field0.level) {
            LogLevel.DEBUG -> d(event.field0.message)
            LogLevel.INFO -> i(event.field0.message)
            LogLevel.WARN -> w(event.field0.message)
        }
        is RunEvent.PatchFinished -> when (val status = event.status) {
            is PatchStatus.Failed -> e("${event.patch} failed: ${status.reason}")
            is PatchStatus.Skipped -> i("${event.patch} skipped: ${status.reason}")
            else -> d("${event.patch}: $status")
        }
        is RunEvent.PatchesReverted -> i("Reapplying ${event.patches.joinToString()}")
    }

    private companion object {
        // Patches in the Valenced bundle
        const val VALENCED = "Valenced"
        const val PACKAGE_NAME = "Change package name"
        const val APP_NAME = "Change app name"
        const val DEBUGGABLE = "Enable debugging"
        const val ICON = "Valenced icon"
    }

}
