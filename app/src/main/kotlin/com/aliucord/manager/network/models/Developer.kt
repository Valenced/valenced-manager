package com.aliucord.manager.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Developer(
    val username: String,
    val avatarUrl: String,
    val commits: Int,
    val repositories: List<Repository>,
    val role: String? = null
) {
    @Serializable
    data class Repository(
        val name: String,
        val commits: Int
    )
}
