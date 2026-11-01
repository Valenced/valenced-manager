package com.aliucord.manager.ui.screens.about

import com.aliucord.manager.network.models.Developer
import kotlinx.collections.immutable.ImmutableList

sealed interface AboutScreenState {
    data object Loading : AboutScreenState
    data object Failure : AboutScreenState
    data class Loaded(val contributors: ImmutableList<Developer>) : AboutScreenState
}
