/*
 * Copyright (c) 2022 Juby210 & zt
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.manager.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import app.valenced.manager.R

@Composable
fun ManagerTheme(
    theme: Theme = Theme.System,
    dynamicColor: Boolean = true,
    amoled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val darkTheme = when (theme) {
        Theme.System -> isSystemInDarkTheme()
        Theme.Dark -> true
        Theme.Light -> false
    }

    val context = LocalContext.current

    val baseScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val colorScheme = if (amoled && darkTheme) {
        baseScheme.copy(
            // Core surfaces: true black with white content
            background = Color(0xFF000000),
            onBackground = Color(0xFFFFFFFF),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFFFFFFF),

            // Surface variants / tints for containers and subtle surfaces
            surfaceVariant = Color(0xFF000000),
            onSurfaceVariant = Color(0xFFFFFFFF),
            surfaceTint = Color(0xFF000000),

            // Primary (controls). Set both primary and primaryContainer to white so
            // Filled / FilledTonal controls and other components using either token
            // will have a white container with black text/icons.
            primary = Color(0xFFFFFFFF),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFFFFFFFF),
            onPrimaryContainer = Color(0xFF000000),

            // Secondary / tertiary: match primary behavior for consistency across different control types
            secondary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF000000),
            secondaryContainer = Color(0xFFFFFFFF),
            onSecondaryContainer = Color(0xFF000000),

            tertiary = Color(0xFFFFFFFF),
            onTertiary = Color(0xFF000000),
            tertiaryContainer = Color(0xFFFFFFFF),
            onTertiaryContainer = Color(0xFF000000),

            // Inverse surfaces for components that might use them
            inverseSurface = Color(0xFF121212),
            inverseOnSurface = Color(0xFFFFFFFF),

            // Outline and subtle elements: semi-transparent white for edges/highlights
            outline = Color(0x66FFFFFF),

            // Preserve error semantics from base (keeps readable error colors)
            error = baseScheme.error,
            onError = baseScheme.onError,
            errorContainer = baseScheme.errorContainer,
            onErrorContainer = baseScheme.onErrorContainer,
        )
    } else baseScheme

    val customColors = when (darkTheme) {
        true -> DarkCustomColors
        false -> LightCustomColors
    }

    // As usual, Google deprecates accompanist libraries and replaces them with an incomplete and shitty replacement in androidx
    // enableEdgeToEdge() does not work for our use case.
    @Suppress("DEPRECATION")
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,
            darkIcons = !darkTheme,
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
        )
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ThemeTypography,
            content = content,
        )
    }
}

enum class Theme {
    System,
    Light,
    Dark;

    @Composable
    fun toDisplayName() = when (this) {
        System -> stringResource(R.string.theme_system)
        Light -> stringResource(R.string.theme_light)
        Dark -> stringResource(R.string.theme_dark)
    }

    @Composable
    fun toPainter() = when (this) {
        System -> painterResource(R.drawable.ic_sync)
        Light -> painterResource(R.drawable.ic_light)
        Dark -> painterResource(R.drawable.ic_night)
    }
}
