package com.aliucord.manager.patcher.steps.patch

import com.aliucord.manager.network.utils.SemVer
import com.aliucord.manager.patcher.InstallMetadata
import com.aliucord.manager.patcher.StepRunner
import com.aliucord.manager.patcher.steps.StepGroup
import com.aliucord.manager.patcher.steps.base.Step
import com.aliucord.manager.patcher.steps.download.*
import com.aliucord.manager.ui.screens.patchopts.PatchOptions
import com.github.diamondminer88.zip.ZipWriter
import app.valenced.manager.BuildConfig
import app.valenced.manager.R
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lsposed.lspatch.share.LSPConfig

/**
 * Store the install options and additional data inside the APK for future use,
 * for example checking what library versions were used, or performing "updates" while
 * maintaining the same install options as what was used upon first install.
 */
class SaveMetadataStep(private val options: PatchOptions) : Step(), KoinComponent {
    private val json: Json by inject()

    override val group = StepGroup.Patch
    override val localizedName = R.string.patch_step_save_metadata

    override suspend fun execute(container: StepRunner) {
        val apk = container.getStep<CopyDependenciesStep>().patchedApk
        val aliuhook = container.getStepOrNull<DownloadAliuhookStep>()
        val injector = container.getStepOrNull<DownloadInjectorStep>()
        val patches = container.getStepOrNull<DownloadPatchesStep>()
        val valencedXposed = container.getStepOrNull<DownloadValencedXposedStep>()

        val metadata = InstallMetadata(
            options = options,
            customManager = !BuildConfig.RELEASE,
            managerVersion = SemVer.parseOrNull(BuildConfig.VERSION_NAME) ?: SemVer(1, 1, 2),
            aliuhookVersion = aliuhook?.targetVersion,
            injectorVersion = injector?.targetVersion,
            patchesVersion = patches?.targetVersion,
            lspatchVersion = LSPConfig.instance.VERSION_CODE,
            valencedXposedVersion = valencedXposed?.targetVersion,
            shiggyXposedVersion = valencedXposed?.targetVersion,
        )

        container.log("Writing serialized install metadata to APK")
        ZipWriter(apk, /* append = */ true).use {
            it.writeEntry("valenced.json", json.encodeToString<InstallMetadata>(metadata))
        }
    }
}
