package com.aliucord.manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Suppress("UnusedReceiverParameter")
val MaterialTheme.customColors: CustomColors
    @Composable
    inline get() = LocalCustomColors.current

val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No LocalCustomColors provided!")
}

@Immutable
data class CustomColors(
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val instanceCardBackground: Color,
    val instanceCardContent: Color,
    val instanceCardButtonContainer: Color,
    val instanceCardButtonContent: Color,
)

private val YellowAlt1 = Color(0xFFE9C414)
private val Shandy = Color(0xFFFFE172)
private val DarkBrown = Color(0xFF3B2F00)
private val DarkerBrown = Color(0xFF221B00)
private val DarkBronze = Color(0xFF554600)

val DarkCustomColors = CustomColors(
    warning = YellowAlt1,
    onWarning = DarkBrown,
    warningContainer = DarkBronze,
    onWarningContainer = Shandy,
    instanceCardBackground = Color(0xFF0E0E0E),
    instanceCardContent = Color(0xFFFFFFFF),
    instanceCardButtonContainer = Color(0xFFF2F2F2),
    instanceCardButtonContent = Color(0xFF000000),
)

val LightCustomColors = CustomColors(
    warning = YellowAlt1,
    onWarning = Color.White,
    warningContainer = Shandy,
    onWarningContainer = DarkerBrown,
    instanceCardBackground = Color(0xFFFFFFFF),
    instanceCardContent = Color(0xFF000000),
    instanceCardButtonContainer = Color(0xFFFAFAFA),
    instanceCardButtonContent = Color(0xFF000000),
)
