package com.aliucord.manager.patcher

import com.aliucord.manager.network.utils.SemVer
import com.aliucord.manager.ui.screens.patchopts.PatchOptions
import kotlinx.serialization.Serializable

/**
 * Data stored inside patched APKs as "valenced.json" in order to preserve install-time information about Valenced and the Manager.
 */
@Serializable
data class InstallMetadata(
    /**
     * The user-selected options for this installation.
     */
    val options: PatchOptions,

    /**
     * Whether the manager is a real release or built from source.
     */
    val customManager: Boolean,

    /**
     * The semver version of this manager that performed the installation.
     */
    val managerVersion: SemVer,

    /**
     * Version (commit hash) of the hook build injected into the APK.
     */
    val aliuhookVersion: SemVer? = null,

    /**
     * Version of the injector build injected into the APK.
     */
    val injectorVersion: SemVer? = null,

    /**
     * Version of the smali patches applied onto the APK.
     */
    val patchesVersion: SemVer? = null,

    /**
     * Version of LSPatch used to inject Xposed module into the APK.
     */
    val lspatchVersion: Int? = null,

    /**
     * Version of Valenced Xposed embedded into the APK.
     */
    val valencedXposedVersion: SemVer? = null,

    /**
     * Legacy field for backward compatibility with older installs.
     */
    val shiggyXposedVersion: SemVer? = null,
) {
    val xposedVersion: SemVer?
        get() = valencedXposedVersion ?: shiggyXposedVersion
}
