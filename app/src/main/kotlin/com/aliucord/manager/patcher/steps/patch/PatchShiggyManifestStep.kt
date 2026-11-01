package com.aliucord.manager.patcher.steps.patch

import com.aliucord.manager.patcher.util.ManifestPatcher
import com.aliucord.manager.ui.screens.patchopts.PatchOptions

class PatchShiggyManifestStep(val options: PatchOptions) : PatchManifestStep(options) {
    override fun patchManifest(manifestBytes: ByteArray, isBase: Boolean) = ManifestPatcher.patchManifest(
        manifestBytes = manifestBytes,
        packageName = options.packageName,
        compileSdkVersion = null,
        compileSdkVersionCodename = null,
        targetSdkVersion = null,
        useEmbeddedDex = false,
        requestLegacyExternalStorage = false,
        appName = if (isBase) options.appName else null,
        debuggable = options.debuggable,
    )
}
