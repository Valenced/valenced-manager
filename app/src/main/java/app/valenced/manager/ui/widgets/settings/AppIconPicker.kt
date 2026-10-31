package app.valenced.manager.ui.widgets.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.valenced.manager.domain.manager.LauncherIcon
import app.valenced.manager.ui.widgets.AppIcon
import app.valenced.manager.utils.DiscordVersion
import java.io.File

/**
 * @param customIcon The picked image, if [hasCustomIcon]
 * @param onPickColor Called to pick the [LauncherIcon.COLOR] background, which selects it
 * @param onPickCustom Called to pick a new image, when there's none yet or the custom icon is tapped while selected
 */
@Composable
fun AppIconPicker(
    selected: LauncherIcon,
    customIcon: File,
    hasCustomIcon: Boolean,
    customIconVersion: Int,
    color: Color,
    releaseChannel: DiscordVersion.Type?,
    onSelect: (LauncherIcon) -> Unit,
    onPickColor: () -> Unit,
    onPickCustom: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .selectableGroup()
    ) {
        for (icon in LauncherIcon.entries) {
            val isSelected = selected == icon

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .selectable(isSelected) {
                        when {
                            icon == LauncherIcon.COLOR -> onPickColor()
                            icon == LauncherIcon.CUSTOM && (isSelected || !hasCustomIcon) -> onPickCustom()
                            else -> onSelect(icon)
                        }
                    }
                    .padding(8.dp)
            ) {
                val iconModifier = Modifier
                    .size(56.dp)
                    .run {
                        if (isSelected) border(BorderStroke(4.dp, MaterialTheme.colorScheme.tertiary), CircleShape)
                        else this
                    }

                if (icon == LauncherIcon.CUSTOM && !hasCustomIcon) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = iconModifier
                            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null // Covered by the label
                        )
                    }
                } else key(customIconVersion) {
                    AppIcon(
                        icon = icon,
                        customIcon = customIcon,
                        color = color,
                        releaseChannel = releaseChannel,
                        modifier = iconModifier
                    )
                }

                Text(
                    text = stringResource(icon.labelRes),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
