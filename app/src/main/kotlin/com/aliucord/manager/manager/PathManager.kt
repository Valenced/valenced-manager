package com.aliucord.manager.manager

import android.content.Context
import android.os.Environment
import com.aliucord.manager.network.utils.SemVer
import java.io.File

/**
 * A central place to provide all system paths that are used.
 */
class PathManager(
    private val context: Context,
) {
    /**
     * The Valenced folder where shared files are stored.
     * Standard path: `~/Valenced`
     */
    val valencedDir = Environment.getExternalStorageDirectory().resolve("Valenced")

    /**
     * Legacy folder fallback.
     */
    val shiggyDir = Environment.getExternalStorageDirectory().resolve("Shiggy")

    /**
     * Global keystore used for signing APKs.
     */
    val keystoreFile = if (valencedDir.resolve("ks.keystore").exists()) {
        valencedDir.resolve("ks.keystore")
    } else if (shiggyDir.resolve("ks.keystore").exists()) {
        shiggyDir.resolve("ks.keystore")
    } else {
        valencedDir.resolve("ks.keystore")
    }

    /**
     * The internal directory used for downloading components related to patching, and
     * running the patching process itself.
     *
     * This should not be a cache dir provided by Android, since it will be wiped when
     * the device is low on storage, and result in a failed patching process.
     */
    val patchingDir = context.filesDir.resolve("patching").apply { mkdirs() }

    /**
     * The internal app directory uses for downloads that should not be wiped,
     * to be used during the patching process. When the process completes, then this
     * is to be moved to the cache dir, to allow Android to wipe the downloads when
     * low on storage.
     */
    val patchingDownloadDir = patchingDir.resolve("downloads")

    /**
     * Used as a secondary location for downloads when not currently patching.
     * This allows Android to clear the download cache when low on storage.
     */
    val cacheDownloadDir = context.cacheDir.resolve("downloads")

    /**
     * Directory used for logs created during the installation process.
     */
    val logsDir = context.filesDir.resolve("logs")

    /**
     * A permanent location used for storing custom patching components.
     * This does not get moved to an Android-managed cache dir when
     * not currently patching.
     */
    val customComponentsDir = patchingDir.resolve("custom")

    /**
     * Directory where custom local components can be placed.
     */
    val customDir = valencedDir.resolve("custom")

    /**
     * Delete all the cache dirs and recreate them.
     */
    fun clearCache() {
        for (dir in arrayOf(patchingDir, cacheDownloadDir, context.cacheDir, context.externalCacheDir)) {
            dir?.deleteRecursively()
            dir?.mkdirs()
        }
    }

    /**
     * Create a new subfolder in the Discord APK cache for a specific version.
     */
    fun discordApkVersionCache(version: Int): File = patchingDownloadDir
        .resolve("discord")
        .resolve(version.toString())
        .apply { mkdirs() }

    /**
     * Resolve a specific path for a cached injector.
     */
    fun cachedInjectorDex(version: SemVer, custom: Boolean = false) = patchingDownloadDir
        .resolve("injector").apply { mkdirs() }
        .resolve("$version${if (custom) ".custom" else ""}.dex")

    /**
     * Get all the versions of custom injector builds.
     */
    fun customInjectorDexs() = listCustomFiles(customComponentsDir.resolve("injector"))

    /**
     * Resolve a specific path for a versioned cached Aliuhook build
     */
    fun cachedAliuhookAAR(version: SemVer) = patchingDownloadDir
        .resolve("aliuhook").apply { mkdirs() }
        .resolve("$version.aar")

    /**
     * Resolve a specific path for a versioned smali patches archive.
     */
    fun cachedSmaliPatches(version: SemVer, custom: Boolean = false) = patchingDownloadDir
        .resolve("patches").apply { mkdirs() }
        .resolve("$version${if (custom) ".custom" else ""}.zip")

    /**
     * Get all the versions of custom smali bundles.
     */
    fun customSmaliPatches() = listCustomFiles(customComponentsDir.resolve("patches"))

    /**
     * Singular Kotlin file of the most up-to-date version
     * since the stdlib is backwards compatible.
     */
    fun cachedKotlinDex() = patchingDownloadDir
        .resolve("kotlin.dex")

    fun cachedValencedXposed(version: SemVer) = patchingDownloadDir
        .resolve("wt-xposed")
        .resolve("$version.apk")

    fun cachedShiggyXposed(version: SemVer) = cachedValencedXposed(version)

    fun customKotlin() = customDir.resolve("kotlin.dex")
    fun customInjector() = listCustomFiles(customDir.resolve("injector"))
    fun customAliuhook() = listCustomFiles(customDir.resolve("aliuhook"))
    fun customPatches() = listCustomFiles(customDir.resolve("patches"))
    fun customSmali() = customDir.resolve("smali")

    fun cachedBaseApk(version: Int) = patchingDownloadDir
        .resolve("discord-$version-base.apk")

    fun cachedLanguageApk(version: Int) = patchingDownloadDir
        .resolve("discord-$version-lang.apk")

    fun cachedResourcesApk(version: Int) = patchingDownloadDir
        .resolve("discord-$version-resources.apk")

    fun cachedLibApk(version: Int) = patchingDownloadDir
        .resolve("discord-$version-lib.apk")

    fun cachedAliuhook(version: SemVer) = patchingDownloadDir
        .resolve("aliuhook")
        .resolve("$version.zip")

    fun cachedInjector(version: SemVer) = patchingDownloadDir
        .resolve("injector")
        .resolve("$version.zip")

    fun cachedPatches(version: SemVer) = patchingDownloadDir
        .resolve("patches")
        .resolve("$version.dex")

    /**
     * The temporary working directory of a currently executing patching process.
     */
    fun patchingWorkingDir() = patchingDir
        .resolve("patched")

    /**
     * The APK that is worked on during the patching process.
     */
    fun patchedApk(part: String = "base") = patchingWorkingDir().resolve("patched-$part.apk")

    private companion object {
        /**
         * List all the files in a directory that follow SemVer naming.
         */
        private fun listCustomFiles(dir: File): List<SemVer>? {
            if (!dir.exists()) return null

            val files = dir.listFiles()
                ?: throw Error("Failed to list directory")

            return files
                .map { it.nameWithoutExtension }
                .mapNotNull(SemVer.Companion::parseOrNull)
        }
    }
}
