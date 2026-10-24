package app.valenced.manager.domain.manager

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.reseam.sdk.Compatibility
import app.reseam.sdk.InspectRequest
import app.reseam.sdk.Trust
import app.reseam.sdk.inspect
import app.valenced.manager.utils.DiscordVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The Valenced patch bundle shipped in this app's assets. Its patches are pinned to one Discord
 * version, which is the version this app installs.
 */
class BundleManager(private val context: Context) {

    private val mutex = Mutex()
    private var bundle: PatchBundle? = null

    /** The Discord version the bundle is pinned to, once [load] has read it. */
    var discordVersion by mutableStateOf<DiscordVersion?>(null)
        private set

    suspend fun load(): PatchBundle = mutex.withLock {
        bundle ?: withContext(Dispatchers.IO) { read() }.also {
            bundle = it
            discordVersion = it.discordVersion
        }
    }

    private fun read(): PatchBundle {
        val file = context.filesDir.resolve(ASSET)
        context.assets.open(ASSET).use { input -> file.outputStream().use { input.copyTo(it) } }
        val inspection = inspect(
            InspectRequest(splitPaths = emptyList(), bundlePaths = listOf(file.absolutePath), trust = Trust(keys = emptyList()))
        )
        val versions = inspection.patches
            .flatMap { (it.spec.compatibility as? Compatibility.Packages)?.packages.orEmpty() }
            .filter { it.`package` == DISCORD_PACKAGE }
            .flatMap { it.versions }
            .toSet()
        val version = versions.singleOrNull() ?: error("The bundle must pin one Discord version, found $versions")
        return PatchBundle(
            file = file,
            signer = inspection.bundles.single().publicKey,
            discordVersion = DiscordVersion.fromVersionName(version)
                ?: error("The bundle pins an unknown Discord version: $version"),
        )
    }

    private companion object {
        const val ASSET = "valenced.reseam"
        const val DISCORD_PACKAGE = "com.discord"
    }
}

/**
 * @param signer The key the bundle was signed with. It ships inside this app, so it is as trusted
 * as the app.
 */
data class PatchBundle(
    val file: File,
    val signer: String,
    val discordVersion: DiscordVersion,
)
