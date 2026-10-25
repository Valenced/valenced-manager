package app.valenced.manager.domain.manager

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.reseam.sdk.Compatibility
import app.reseam.sdk.InspectRequest
import app.reseam.sdk.Problem
import app.reseam.sdk.Trust
import app.reseam.sdk.inspect
import app.valenced.manager.BuildConfig
import app.valenced.manager.domain.repository.RestRepository
import app.valenced.manager.network.dto.Release
import app.valenced.manager.network.utils.dataOrNull
import app.valenced.manager.utils.DiscordVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The Valenced patch bundle from the latest Valenced release. Its patches are pinned to one Discord
 * version, which is the version this app installs, and the Xposed module comes from the same
 * release so the two always match.
 *
 * The last bundle that could be used is kept, so installing still works offline or when the latest
 * release can't be used, such as one built for a newer Reseam engine than this app's.
 */
class BundleManager(
    private val context: Context,
    private val repo: RestRepository,
    private val downloadManager: DownloadManager,
    private val prefs: PreferenceManager,
) {

    private val mutex = Mutex()

    /** The bundle [load] read, if it has. */
    var bundle: PatchBundle? = null
        private set

    private val file get() = context.filesDir.resolve(BUNDLE_ASSET)

    /** The Discord version the bundle is pinned to, once [load] has read it. */
    var discordVersion by mutableStateOf<DiscordVersion?>(null)
        private set

    /** Why [load] last failed, if it did. */
    var problem by mutableStateOf<String?>(null)
        private set

    /** @param refresh Whether to check for a newer release rather than reuse the bundle already read. */
    suspend fun load(refresh: Boolean = false): PatchBundle = mutex.withLock {
        bundle?.takeUnless { refresh } ?: withContext(Dispatchers.IO) { runCatching { update() } }
            .onSuccess {
                bundle = it
                discordVersion = it.discordVersion
                problem = null
            }
            .onFailure { problem = it.message ?: it.toString() }
            .getOrThrow()
    }

    private suspend fun update(): PatchBundle {
        val custom = prefs.patchBundleLocation.takeIf { prefs.isDeveloper && it.isNotBlank() }
        if (custom != null) {
            val file = File(custom)
            check(file.isFile) { "No patch bundle at $custom" }
            return read(file, release = null)
        }

        val release = repo.getLatestRelease(BuildConfig.REPO).dataOrNull
        if (release == null || (release.tagName == prefs.moduleVersion && file.exists())) return cached()
        return runCatching { download(release) }.getOrElse { error ->
            Log.w(TAG, "Can't use Valenced ${release.tagName}", error)
            runCatching { cached() }.getOrNull() ?: throw error
        }
    }

    private fun cached(): PatchBundle {
        check(file.exists() && prefs.moduleVersion.isNotEmpty()) { "Couldn't download Valenced patches" }
        return read(file, prefs.moduleVersion)
    }

    /** Downloads [release]'s bundle and, if it can be used, makes it and its module current. */
    private suspend fun download(release: Release): PatchBundle {
        val url = release.assets.find { it.name == BUNDLE_ASSET }?.browserDownloadUrl
            ?: error("Valenced ${release.tagName} has no patch bundle")
        val download = file.resolveSibling("$BUNDLE_ASSET.download")
        try {
            when (val result = downloadManager.download(url, download) {}) {
                is DownloadResult.Error -> error("Couldn't download Valenced patches: ${result.debugReason}")
                DownloadResult.Cancelled -> error("Downloading Valenced patches was cancelled")
                DownloadResult.Success -> {}
            }
            val bundle = read(download, release.tagName)
            download.copyTo(file, overwrite = true)
            // The downloaded module is the previous release's. One the user picked is theirs to keep.
            prefs.DEFAULT_MODULE_LOCATION.delete()
            prefs.moduleVersion = release.tagName
            return bundle.copy(file = file)
        } finally {
            download.delete()
        }
    }

    /** @param release [file]'s release, or null for a custom bundle. */
    private fun read(file: File, release: String?): PatchBundle {
        val name = release?.let { "Valenced $it" } ?: file.name
        val inspection = inspect(
            InspectRequest(
                splitPaths = emptyList(),
                bundlePaths = listOf(file.absolutePath),
                trust = Trust(keys = listOfNotNull(BuildConfig.BUNDLE_PUBLIC_KEY.takeIf { release != null })),
            )
        )
        val metadata = inspection.bundles.single()
        when (val problem = metadata.problem) {
            null -> {}
            is Problem.BundleTooOld ->
                error("$name was built for Reseam ${problem.built}, this Manager runs ${problem.running}")
            is Problem.EngineTooOld ->
                error("$name needs Reseam ${problem.built}, update Valenced Manager")
            else -> error("$name's patches can't be read: $problem")
        }
        check(metadata.trusted || release == null) { "$name's patches are signed by an unknown key ${metadata.publicKey}" }

        val versions = inspection.patches
            .flatMap { (it.spec.compatibility as? Compatibility.Packages)?.packages.orEmpty() }
            .filter { it.`package` == DISCORD_PACKAGE }
            .flatMap { it.versions }
            .toSet()
        val version = versions.singleOrNull() ?: error("The bundle must pin one Discord version, found $versions")
        return PatchBundle(
            file = file,
            signer = metadata.publicKey,
            release = release,
            discordVersion = DiscordVersion.fromVersionName(version)
                ?: error("The bundle pins an unknown Discord version: $version"),
        )
    }

    private companion object {
        const val TAG = "BundleManager"
        const val BUNDLE_ASSET = "valenced.reseam"
        const val DISCORD_PACKAGE = "com.discord"
    }
}

/**
 * @param signer The key the bundle was signed with, unless [custom].
 * @param release The Valenced release the patch bundle is from to load the Xposed module with. `null` for a custom bundle, which will use the latest release's.
 */
data class PatchBundle(
    val file: File,
    val signer: String,
    val release: String?,
    val discordVersion: DiscordVersion,
) {
    val custom get() = release == null

    val moduleUrl get() = "https://github.com/${BuildConfig.REPO}/releases/" +
        (release?.let { "download/$it" } ?: "latest/download") + "/app-release.apk"
}
