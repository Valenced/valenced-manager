package com.aliucord.manager.patcher.steps.patch

import com.aliucord.manager.patcher.StepRunner
import com.aliucord.manager.patcher.steps.StepGroup
import com.aliucord.manager.patcher.steps.base.Step
import com.aliucord.manager.patcher.steps.download.CopyDependenciesStep
import com.aliucord.manager.patcher.util.ManifestPatcher
import com.aliucord.manager.ui.screens.patchopts.PatchOptions
import com.github.diamondminer88.zip.ZipReader
import com.github.diamondminer88.zip.ZipWriter
import app.valenced.manager.R

/**
 * Patch the APK's AndroidManifest.xml
 */
open class PatchManifestStep(private val options: PatchOptions) : Step() {
    override val group = StepGroup.Patch
    override val localizedName = R.string.patch_step_patch_manifests

    protected open fun patchManifest(manifestBytes: ByteArray, isBase: Boolean) = ManifestPatcher.patchManifest(
        manifestBytes = manifestBytes,
        packageName = options.packageName,
        appName = options.appName,
        debuggable = options.debuggable,
    )

    override suspend fun execute(container: StepRunner) {
        val dependenciesStep = container.getStep<CopyDependenciesStep>()
        for (apk in dependenciesStep.patchedApks) {
            container.log("Reading manifest from apk")
            val manifestBytes = ZipReader(apk)
                .use { zip -> zip.openEntry("AndroidManifest.xml")?.read() }
                ?: throw IllegalArgumentException("No manifest found in APK")

            container.log("Patching manifest")
            val isBase = apk == dependenciesStep.patchedApk
            val patchedManifest = patchManifest(manifestBytes, isBase)

            container.log("Writing patched manifest to apk unaligned compressed")
            ZipWriter(apk, /* append = */ true).use {
                it.deleteEntry("AndroidManifest.xml", apk == dependenciesStep.patchedLibApk || apk == dependenciesStep.patchedApk)
                it.writeEntry("AndroidManifest.xml", patchedManifest)
            }
        }
    }
}
