package app.revenge.manager.installer.step.patching

import android.content.Context
import app.reseam.sdk.InspectRequest
import app.reseam.sdk.LogLevel
import app.reseam.sdk.OptionValue
import app.reseam.sdk.PatchOutput
import app.reseam.sdk.PatchRequest
import app.reseam.sdk.PatchSelection
import app.reseam.sdk.PatchStatus
import app.reseam.sdk.RunEvent
import app.reseam.sdk.Trust
import app.reseam.sdk.inspect
import app.reseam.sdk.patch
import app.revenge.manager.BuildConfig
import app.revenge.manager.R
import app.revenge.manager.domain.manager.PreferenceManager
import app.revenge.manager.installer.step.Step
import app.revenge.manager.installer.step.StepGroup
import app.revenge.manager.installer.step.StepRunner
import app.revenge.manager.installer.step.download.DownloadBaseStep
import app.revenge.manager.installer.step.download.DownloadLangStep
import app.revenge.manager.installer.step.download.DownloadLibsStep
import app.revenge.manager.installer.step.download.DownloadModStep
import app.revenge.manager.installer.step.download.DownloadResourcesStep
import app.revenge.manager.installer.util.Logger
import app.revenge.manager.installer.util.Signer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.io.File

/**
 * Patches Discord with the Revenge bundle using Reseam, then signs it
 *
 * @param workingDir Where the bundle is unpacked from the app's assets
 * @param outputDir Where the patched and signed apks are written
 */
class PatchStep(
    private val workingDir: File,
    private val outputDir: File
) : Step() {

    private val preferences: PreferenceManager by inject()
    private val context: Context by inject()

    override val group = StepGroup.PATCHING
    override val nameRes = R.string.step_patch

    override suspend fun run(runner: StepRunner) {
        val baseApk = runner.getCompletedStep<DownloadBaseStep>().workingCopy
        val libsApk = runner.getCompletedStep<DownloadLibsStep>().workingCopy
        val langApk = runner.getCompletedStep<DownloadLangStep>().workingCopy
        val resApk = runner.getCompletedStep<DownloadResourcesStep>().workingCopy
        val module = runner.getCompletedStep<DownloadModStep>().workingCopy

        val bundle = workingDir.resolve(BUNDLE_ASSET)
        context.assets.open(BUNDLE_ASSET).use { input -> bundle.outputStream().use { input.copyTo(it) } }

        val request = PatchRequest(
            apkPath = baseApk.absolutePath,
            splitPaths = listOf(libsApk, langApk, resApk).map { it.absolutePath },
            bundlePaths = listOf(bundle.absolutePath),
            trust = Trust(keys = listOf(signer(bundle))),
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

    /**
     * The key the bundle was signed with. It ships inside this app, so it is as trusted as the app.
     */
    private fun signer(bundle: File) = inspect(
        InspectRequest(splitPaths = emptyList(), bundlePaths = listOf(bundle.absolutePath), trust = Trust(keys = emptyList()))
    ).bundles.single().publicKey

    private fun selection(module: File) = PatchSelection(
        enable = listOfNotNull(DEBUGGABLE.takeIf { preferences.debuggable }),
        disable = listOfNotNull(ICON.takeUnless { preferences.patchIcon }),
        options = mapOf(
            REVENGE to mapOf("module" to OptionValue.Path(module.absolutePath)),
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
        const val BUNDLE_ASSET = "revenge.reseam"

        // Patches in the Revenge bundle
        const val REVENGE = "Revenge"
        const val PACKAGE_NAME = "Change package name"
        const val APP_NAME = "Change app name"
        const val DEBUGGABLE = "Enable debugging"
        const val ICON = "Revenge icon"
    }

}
