package com.aliucord.manager.patcher.steps.download

import android.os.Build
import com.aliucord.manager.manager.PathManager
import com.aliucord.manager.network.services.RNATrackerService.Companion.BASE_URL
import com.aliucord.manager.patcher.steps.base.DownloadDiscordRNAStep
import app.valenced.manager.R
import org.koin.core.component.inject

class DownloadDiscordRNALibStep : DownloadDiscordRNAStep() {
    private val paths: PathManager by inject()
    private val arch = Build.SUPPORTED_ABIS.first().replace("-v", "_v")

    override val targetUrl get() = "$BASE_URL/tracker/download/$version/config.$arch"
    override val targetFile get() = paths.discordApkVersionCache(version).resolve("config.$arch.apk")
    override val localizedName = R.string.patch_step_dl_libapk
}
