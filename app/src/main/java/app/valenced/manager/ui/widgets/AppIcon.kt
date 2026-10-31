package app.valenced.manager.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.valenced.manager.BuildConfig
import app.valenced.manager.R
import app.valenced.manager.domain.manager.LauncherIcon
import app.valenced.manager.utils.DiscordVersion
import coil.compose.AsyncImage
import java.io.File

@Composable
fun AppIcon(
    icon: LauncherIcon,
    customIcon: File,
    color: Color,
    releaseChannel: DiscordVersion.Type?,
    modifier: Modifier = Modifier
) {
    if (icon == LauncherIcon.CUSTOM) {
        AsyncImage(
            model = customIcon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(CircleShape)
                .background(Color.Black)
                // Launchers show the middle two thirds of the icon layer
                .graphicsLayer {
                    scaleX = 1.5f
                    scaleY = 1.5f
                }
        )
        return
    }

    val iconColor = remember(icon, color, releaseChannel) {
        when {
            icon == LauncherIcon.COLOR -> color
            icon == LauncherIcon.VALENCED -> Color(BuildConfig.MODDED_APP_ICON)
            releaseChannel == DiscordVersion.Type.ALPHA -> Color(BuildConfig.MODDED_APP_ICON_ALPHA)
            else -> Color(BuildConfig.MODDED_APP_ICON_OTHER)
        }
    }

    Image(
        painter = painterResource(id = R.drawable.ic_discord_icon),
        contentDescription = null,
        colorFilter = if (icon == LauncherIcon.COLOR) ColorFilter.tint(LauncherIcon.logoColor(color)) else null,
        modifier = modifier
            .clip(CircleShape)
            .background(iconColor)
    )
}
