package com.aliucord.manager.network.models

import kotlinx.serialization.Serializable

@Serializable
data class RNATrackerIndex(
    val latest: Versions,
) {
    @Serializable
    data class Versions(
        val alpha: Int,
        val beta: Int,
        val stable: Int,
    )
}
