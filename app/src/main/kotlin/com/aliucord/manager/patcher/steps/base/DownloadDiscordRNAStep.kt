package com.aliucord.manager.patcher.steps.base

import com.aliucord.manager.patcher.StepRunner
import com.aliucord.manager.patcher.steps.prepare.FetchDiscordRNAStep
import kotlin.properties.Delegates

abstract class DownloadDiscordRNAStep : DownloadStep() {
    protected var version by Delegates.notNull<Int>()
        private set

    override suspend fun execute(container: StepRunner) {
        version = container.getStep<FetchDiscordRNAStep>().targetVersion
        super.execute(container)
    }
}
