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
                username = "Rosie",
                avatarUrl = "https://github.com/Rosie.png",
                commits = 0,
                repositories = emptyList(),
                role = "Valenced - Lead Developer"
            ),
            Developer(
                username = "rushiiMachine",
                avatarUrl = "https://github.com/rushiiMachine.png",
                commits = 0,
                repositories = emptyList(),
                role = "Manager - Contributor"
            )
        )

        mutableState.value = AboutScreenState.Loaded(allDevelopers.toUnsafeImmutable())
    }
}
