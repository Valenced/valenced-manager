/*
 * Copyright (c) 2022 Juby210 & zt
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.manager.ui.screens.about

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.aliucord.manager.ui.components.*
import com.aliucord.manager.ui.screens.about.components.ContributorListItem
import com.aliucord.manager.ui.util.paddings.*
import app.valenced.manager.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class AboutScreen : Screen, Parcelable {
    @IgnoredOnParcel override val key = "About"

    @Composable
    override fun Content() {
        val model = koinScreenModel<AboutModel>()

        AboutScreenContent(state = model.state.collectAsState())
    }
}

@Composable
fun AboutScreenContent(state: State<AboutScreenState>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.navigation_about_manager)) },
                navigationIcon = { BackButton() },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = paddingValues
                .exclude(PaddingValuesSides.Horizontal + PaddingValuesSides.Top)
                .add(PaddingValues(vertical = 16.dp)),
            modifier = Modifier
                .padding(paddingValues.exclude(PaddingValuesSides.Bottom))
        ) {
            item(key = "MANAGER_CREDIT_DISCLAIMER_BANNER") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.large
                        )
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Text(
                        text = stringResource(R.string.about_manager_credit_disclaimer_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }

            item(key = "PROJECT_HEADER") {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ProjectHeader()
                }
            }

            item(key = "TEAM_SPACER") {
                Spacer(modifier = Modifier.height(32.dp))
            }

            when (val currentState = state.value) {
                AboutScreenState.Loading -> {
                    item(key = "LOADING") {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                AboutScreenState.Failure -> {
                    item(key = "LOAD_FAILURE") {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp)
                        ) {
                            LoadFailure()
                        }
                    }
                }
                is AboutScreenState.Loaded -> {
                    itemsIndexed(
                        items = currentState.contributors,
                        key = { index, developer -> developer.username }
                    ) { index, developer ->
                        ContributorListItem(
                            contributor = developer,
                            isLast = index == currentState.contributors.lastIndex
                        )
                    }
                }
            }
        }
    }
}
