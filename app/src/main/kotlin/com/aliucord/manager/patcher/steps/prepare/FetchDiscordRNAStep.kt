package com.aliucord.manager.patcher.steps.prepare

import com.aliucord.manager.network.services.RNATrackerService
import com.aliucord.manager.network.utils.getOrThrow
import com.aliucord.manager.patcher.StepRunner
import com.aliucord.manager.patcher.steps.StepGroup
import com.aliucord.manager.patcher.steps.base.Step
import com.aliucord.manager.ui.screens.patchopts.PatchOptions
import com.aliucord.manager.ui.screens.patchopts.VersionPreference
import app.valenced.manager.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.properties.Delegates

class FetchDiscordRNAStep(val options: PatchOptions) : Step(), KoinComponent {
    private val rnaTrackerService: RNATrackerService by inject()

    override val group: StepGroup = StepGroup.Prepare
    override val localizedName: Int = R.string.patch_step_fetch_rna

    var targetVersion by Delegates.notNull<Int>()
        private set

    override suspend fun execute(container: StepRunner) {
        if (options.versionPreference == VersionPreference.Custom) {
            container.log("Custom version selected, skipping fetch.")
            targetVersion = options.customVersionCode.toInt()
            return
        }

        val latestVal = rnaTrackerService.getLatestDiscordVersions().getOrThrow()
        container.log("Fetched latest discord info: $latestVal")

        targetVersion = when (options.versionPreference) {
            VersionPreference.Stable -> latestVal.latest.stable
            VersionPreference.Beta -> latestVal.latest.beta
            VersionPreference.Alpha -> latestVal.latest.alpha
            else -> throw IllegalArgumentException("Unsupported version preference: ${options.versionPreference}")
        }

        container.log("Selected Discord version: $targetVersion")
    }
}
