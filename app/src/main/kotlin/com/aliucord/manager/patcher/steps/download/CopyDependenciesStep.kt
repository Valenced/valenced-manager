package com.aliucord.manager.patcher.steps.download

import android.app.Application
import android.os.Build
import android.os.storage.StorageManager
import androidx.core.content.getSystemService
import com.aliucord.manager.manager.PathManager
import com.aliucord.manager.patcher.StepRunner
import com.aliucord.manager.patcher.steps.StepGroup
import com.aliucord.manager.patcher.steps.base.Step
import com.aliucord.manager.patcher.util.InsufficientStorageException
import app.valenced.manager.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.IOException

/**
 * Step to duplicate the Discord APK to be worked on.
 */
class CopyDependenciesStep : Step(), KoinComponent {
    private val paths: PathManager by inject()
    private val application: Application by inject()

    /**
     * The target APK files which can be modified during patching
     */
    val patchedApk: File = paths.patchedApk()
    var patchedLangApk: File? = null
    var patchedLibApk: File? = null
    var patchedResApk: File? = null

    val patchedApks
        get() = listOfNotNull(
            patchedApk,
            patchedLangApk.takeIf { it?.exists() == true },
            patchedLibApk.takeIf { it?.exists() == true },
            patchedResApk.takeIf { it?.exists() == true }
        )

    override val group = StepGroup.Download
    override val localizedName = R.string.patch_step_copy_deps

    override suspend fun execute(container: StepRunner) {
        val srcApk = container.getStepOrNull<DownloadDiscordRNABaseStep>()?.targetFile ?: container.getStepOrNull<DownloadDiscordStep>()!!.targetFile
        val langApk = container.getStepOrNull<DownloadDiscordRNALangStep>()?.targetFile
        val libApk = container.getStepOrNull<DownloadDiscordRNALibStep>()?.targetFile
        val resApk = container.getStepOrNull<DownloadDiscordRNAResourcesStep>()?.targetFile

        val dir = paths.patchingWorkingDir()

        container.log("Clearing patched directory")
        if (!dir.deleteRecursively())
            throw Error("Failed to clear existing patched dir")

        // Preallocate space for file copy and future patching operations
        if (Build.VERSION.SDK_INT >= 26) {
            val storageManager = application.getSystemService<StorageManager>()!!
            val targetFileStorageId = storageManager.getUuidForPath(patchedApk)
            val fileSize = srcApk.length() + (langApk?.length() ?: 0) + (libApk?.length() ?: 0) + (resApk?.length() ?: 0)

            // We request 3.5x the size of the APK, to give space for the following:
            // 1) A copy of the APK
            // 2) Modifying the copied APK (whether this is necessary I'm not sure)
            // 2) Extracting native libs and other various operations
            val allocSize = (fileSize * 3.5).toLong()

            try {
                storageManager.allocateBytes(targetFileStorageId, allocSize)
            } catch (e: IOException) {
                throw InsufficientStorageException(e.message)
            }
        }

        fun copyApkSafely(src: File?, part: String, onCopied: (File) -> Unit) {
            src?.let {
                container.log("Copying patched apk from ${it.absolutePath} to $part")
                val destFile = File(dir, paths.patchedApk(part).name)
                it.copyTo(destFile)
                onCopied(destFile)
            }
        }

        // Base APK
        container.log("Copying patched base apk from ${srcApk.absolutePath} to ${patchedApk.absolutePath}")
        srcApk.copyTo(patchedApk)

        // Optional APKs
        copyApkSafely(langApk, "lang") { patchedLangApk = it }
        copyApkSafely(libApk, "lib") { patchedLibApk = it }
        copyApkSafely(resApk, "res") { patchedResApk = it }
    }
}
