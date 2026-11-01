package com.aliucord.manager.patcher.steps.download

import com.aliucord.manager.manager.PathManager
import com.aliucord.manager.network.services.RNATrackerService.Companion.BASE_URL
import com.aliucord.manager.patcher.steps.base.DownloadDiscordRNAStep
import app.valenced.manager.R
import org.koin.core.component.inject

class DownloadDiscordRNABaseStep : DownloadDiscordRNAStep() {
    private val paths: PathManager by inject()

    override val targetUrl get() = "$BASE_URL/tracker/download/$version/base"
    override val targetFile get() = paths.discordApkVersionCache(version).resolve("base.apk")
    override val localizedName = R.string.patch_step_dl_baseapk

}
