package com.aliucord.manager.ui.screens.about

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aliucord.manager.network.models.Developer
import com.aliucord.manager.network.services.HttpService
import com.aliucord.manager.ui.util.toUnsafeImmutable
import com.aliucord.manager.util.launchIO
import kotlinx.collections.immutable.persistentListOf

class AboutModel(
    private val http: HttpService,
) : StateScreenModel<AboutScreenState>(AboutScreenState.Loading) {
    init {
        fetchDevelopers()
    }

    fun fetchDevelopers() = screenModelScope.launchIO {
        mutableState.value = AboutScreenState.Loading

        val allDevelopers = persistentListOf(
            Developer(
                username = "kmmiio99o",
                avatarUrl = "https://github.com/kmmiio99o.png",
                commits = 0,
                repositories = emptyList(),
                role = "ShiggyCord - Creator"
            ),
            Developer(
                username = "C0C0B01",
                avatarUrl = "https://github.com/C0C0B01.png",
                commits = 0,
                repositories = emptyList(),
                role = "Kettu - Creator"
            ),
            Developer(
                username = "pylixonly",
                avatarUrl = "https://github.com/pylixonly.png",
                commits = 0,
                repositories = emptyList(),
                role = "Bunny - Creator"
            ),
            Developer(
                username = "maisymoe",
                avatarUrl = "https://github.com/maisymoe.png",
                commits = 0,
                repositories = emptyList(),
                role = "Creator - Vendetta"
            ),
            Developer(
                username = "rushiiMachine",
                avatarUrl = "https://github.com/rushiiMachine.png",
                commits = 0,
                repositories = emptyList(),
                role = "Manager - Creator"
            )
        )

        mutableState.value = AboutScreenState.Loaded(allDevelopers.toUnsafeImmutable())
    }
}
