/*
 * Copyright (c) 2022 Juby210 & zt
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.manager.ui.screens.settings

import android.os.Build
import android.os.Parcelable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.aliucord.manager.manager.InstallerManager
import com.aliucord.manager.ui.components.BackButton
import com.aliucord.manager.ui.components.MainActionButton
import com.aliucord.manager.ui.components.settings.*
import com.aliucord.manager.ui.screens.settings.components.ThemeDialog
import com.aliucord.manager.ui.screens.settings.components.InstallerDialog
import com.aliucord.manager.ui.screens.settings.components.ShizukuInfoDialog
import com.aliucord.manager.manager.InstallerSetting
import com.aliucord.manager.manager.toDisplayName
import app.valenced.manager.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import androidx.compose.ui.platform.LocalContext


@Parcelize
class SettingsScreen : Screen, Parcelable {
    @IgnoredOnParcel
    override val key = "Settings"

    @Composable
    override fun Content() {
        val model = koinScreenModel<SettingsModel>()
        var clearedCache by rememberSaveable { mutableStateOf(false) }


        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.navigation_settings)) },
                    navigationIcon = { BackButton() },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(state = rememberScrollState())
            ) {
                val preferences = model.preferences

                if (model.showThemeDialog) {
                    ThemeDialog(
                        currentTheme = preferences.theme,
                        onDismissRequest = model::hideThemeDialog,
                        onConfirm = model::setTheme
                    )
                }

                SettingsHeader(stringResource(R.string.settings_header_appearance))

                SettingsItem(
                    modifier = Modifier.clickable(onClick = model::showThemeDialog),
                    icon = { Icon(painterResource(R.drawable.ic_brush), null) },
                    text = { Text(stringResource(R.string.setting_theme)) },
                    secondaryText = { Text(stringResource(R.string.setting_theme_desc)) }
                ) {
                    FilledTonalButton(onClick = model::showThemeDialog) {
                        Icon(
                            painter = painterResource(
                                when (preferences.theme) {
                                    com.aliucord.manager.ui.theme.Theme.Dark -> R.drawable.ic_night
                                    com.aliucord.manager.ui.theme.Theme.Light -> R.drawable.ic_light
                                    com.aliucord.manager.ui.theme.Theme.System -> R.drawable.ic_sync
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(18.dp),
                        )
                        Text(preferences.theme.toDisplayName())
                    }
                }


                SettingsSwitch(
                    label = stringResource(R.string.setting_amoled),
                    secondaryLabel = stringResource(R.string.setting_amoled_desc),
                    pref = preferences.amoled,
                    icon = { Icon(painterResource(R.drawable.ic_night), null) },
                    onPrefChange = { preferences.amoled = it },
                )

                // Material You theming on Android 12+
                if (Build.VERSION.SDK_INT >= 31) {
                    SettingsSwitch(
                        label = stringResource(R.string.setting_dynamic_color),
                        secondaryLabel = stringResource(R.string.setting_dynamic_color_desc),
                        pref = preferences.dynamicColor,
                        icon = { Icon(painterResource(R.drawable.ic_palette), null) },
                        onPrefChange = { preferences.dynamicColor = it },
                    )
                }

                SettingsHeader(stringResource(R.string.settings_header_advanced))

                var showInstallerDialog by remember { mutableStateOf(false) }
                var showShizukuInfoDialog by remember { mutableStateOf(false) }
                var shouldRequestShizuku by remember { mutableStateOf(false) }
                val context = LocalContext.current

                if (shouldRequestShizuku) {
                    LaunchedEffect(shouldRequestShizuku) {
                        val SHIZUKU_REQUEST_CODE = 1001
                        try {
                            rikka.shizuku.Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
                            kotlinx.coroutines.delay(800)
                            if (rikka.shizuku.Shizuku.checkSelfPermission() != 0) {
                                showShizukuInfoDialog = true
                            }
                        } catch (_: Exception) {
                            showShizukuInfoDialog = true
                        }
                        shouldRequestShizuku = false
                    }
                }

                if (showInstallerDialog) {
                    InstallerDialog(
                        currentInstaller = preferences.installer,
                        onDismissRequest = { showInstallerDialog = false },
                        onConfirm = { installer ->
                            model.setInstaller(installer)
                            if (installer == InstallerSetting.Shizuku) {
                                shouldRequestShizuku = true
                            }
                        }
                    )
                }
                if (showShizukuInfoDialog) {
                    ShizukuInfoDialog(onDismissRequest = { showShizukuInfoDialog = false })
                }

                SettingsItem(
                    modifier = Modifier.clickable { showInstallerDialog = true },
                    icon = { Icon(painterResource(R.drawable.ic_tools), null) },
                    text = { Text(stringResource(R.string.setting_installer)) },
                    secondaryText = { Text(stringResource(R.string.setting_installer_desc)) }
                ) {
                    FilledTonalButton(onClick = { showInstallerDialog = true }) {
                        Icon(
                            painter = painterResource(
                                when (preferences.installer) {
                                    InstallerSetting.PM -> R.drawable.ic_android
                                    InstallerSetting.ROOT -> R.drawable.ic_hashtag
                                    InstallerSetting.Shizuku -> R.drawable.ic_shizuku
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(18.dp),
                        )
                        Text(preferences.installer.toDisplayName())
                    }
                }

                MainActionButton(
                    text = stringResource(R.string.settings_clear_cache),
                    icon = painterResource(R.drawable.ic_delete_forever),
                    enabled = !clearedCache,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    onClick = {
                        clearedCache = true
                        model.clearCache()
                    },
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .fillMaxWidth()
                )

                SettingsHeader("Development")

                SettingsSwitch(
                    label = stringResource(R.string.setting_developer_options),
                    secondaryLabel = stringResource(R.string.setting_developer_options_desc),
                    pref = preferences.devMode,
                    icon = { Icon(painterResource(R.drawable.ic_code), null) },
                    onPrefChange = { preferences.devMode = it },
                )

                SettingsSwitch(
                    label = stringResource(R.string.setting_keep_patched_apks),
                    secondaryLabel = stringResource(R.string.setting_keep_patched_apks_desc),
                    icon = { Icon(painterResource(R.drawable.ic_delete_forever), null) },
                    pref = preferences.keepPatchedApks,
                    onPrefChange = { model.setKeepPatchedApks(it) },
                    modifier = Modifier.padding(bottom = 18.dp),
                )

                if (preferences.keepPatchedApks) {
                    MainActionButton(
                        text = stringResource(R.string.log_action_export_apk),
                        icon = painterResource(R.drawable.ic_save),
                        enabled = model.patchedApkExists,
                        onClick = model::shareApk,
                        modifier = Modifier
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                            .fillMaxWidth()
                    )
                }

                SettingsHeader(stringResource(R.string.settings_header_info))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 36.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = model.installInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        ),
                    )

                    Spacer(Modifier.weight(1f, fill = true))

                    IconButton(onClick = model::copyInstallInfo) {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy),
                            contentDescription = stringResource(R.string.action_copy),
                            modifier = Modifier
                                .size(28.dp)
                                .alpha(.8f),
                        )
                    }
                }
            }
        }
    }
}
