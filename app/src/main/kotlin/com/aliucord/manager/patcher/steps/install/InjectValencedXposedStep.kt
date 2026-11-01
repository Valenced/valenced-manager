package com.aliucord.manager.patcher.steps.install

import com.aliucord.manager.patcher.StepRunner
import com.aliucord.manager.patcher.steps.StepGroup
import com.aliucord.manager.patcher.steps.base.Step
import com.aliucord.manager.patcher.steps.download.CopyDependenciesStep
import com.aliucord.manager.patcher.steps.download.DownloadValencedXposedStep
import com.aliucord.manager.patcher.util.Signer
import app.valenced.manager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lsposed.lspatch.share.LSPConfig
import org.lsposed.patch.LSPatch
import org.lsposed.patch.util.Logger
import java.io.File
import java.util.*

class InjectValencedXposedStep : Step() {
    override val group: StepGroup = StepGroup.Install
    override val localizedName: Int = R.string.patch_step_inject_shiggy

    suspend fun patch(
        container: StepRunner,
        outputDir: File,
        apkPaths: List<String>,
        embeddedModules: List<String>,
    ) {
        withContext(Dispatchers.IO) {
            LSPatch(
                object : Logger() {
                    override fun d(p0: String?) {
                        container.log("[LSPatch:D] $p0")
                    }

                    override fun e(p0: String?) {
                        container.log("[LSPatch:E] $p0")
                    }

                    override fun i(p0: String?) {
                        container.log("[LSPatch] $p0")
                    }
                },
                *apkPaths.toTypedArray(),
                "-o",
                outputDir.absolutePath,
                "-l",
                "0",
                "-v",
                "-m",
                *embeddedModules.toTypedArray(),
                "-k",
                Signer.getKeystoreFile().absolutePath,
                "password",
                "alias",
                "password"
            ).doCommandLine()
        }
    }

    override suspend fun execute(container: StepRunner) {
        val apks = container.getStep<CopyDependenciesStep>().patchedApks
        val xposed = container.getStep<DownloadValencedXposedStep>().targetFile

        container.log("Adding Valenced module with LSPatch")
        container.log("Valenced module path = ${xposed.absolutePath}")

        // Create temporary folder in working directory
        val tempDir = apks.first().parentFile!!.resolve("lspatched")

        patch(
            container,
            outputDir = tempDir,
            apkPaths = apks.map { it.absolutePath },
            embeddedModules = listOf(xposed.absolutePath)
        )

        // Process each APK and replace with patched version
        apks.forEach { originalApk ->
            val baseName = originalApk.nameWithoutExtension
            // https://github.com/JingMatrix/LSPatch/blob/b98eaf805018c4cc258ded12efe89212a4855e6a/patch/src/main/java/org/lsposed/patch/LSPatch.java#L159-L163
            // String.format(
            //     Locale.getDefault(), "%s-%d-lspatched.apk",
            //     FilenameUtils.getBaseName(apkFileName),
            //     LSPConfig.instance.VERSION_CODE)
            // )
            // Note: LSPatch uses Locale.getDefault() which can produce locale-specific numerals
            // (e.g., Arabic numerals on Arabic locales). We must use Locale.US to ensure
            // consistent filename matching regardless of device locale.
            val patchedApkName = String.format(
                Locale.US,
                "%s-%d-lspatched.apk",
                baseName,
                LSPConfig.instance.VERSION_CODE
            )
            val patchedApk = File(tempDir, patchedApkName)

            if (patchedApk.exists()) {
                patchedApk.copyTo(originalApk, overwrite = true)
                container.log("Replaced ${originalApk.name} with ${patchedApk.name}")
            } else {
                container.log("Warning: Could not find patched APK for ${originalApk.name}")
                container.log("Expected patched APK at: ${patchedApk.absolutePath}")
            }
        }

        tempDir.deleteRecursively()
    }
}
