package app.valenced.manager.ui.widgets.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.valenced.manager.R
import app.valenced.manager.domain.manager.LauncherIcon
import app.valenced.manager.utils.toHex

@Composable
fun IconColorDialog(
    initial: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit,
) {
    val hsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) } }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var brightness by remember { mutableFloatStateOf(hsv[2]) }
    var hex by remember { mutableStateOf(initial.toHex()) }
    val color = Color.hsv(hue, saturation, brightness)

    fun onSlide(update: () -> Unit) {
        update()
        hex = Color.hsv(hue, saturation, brightness).toHex()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.icon_color_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_discord_icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(LauncherIcon.logoColor(color)),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(color)
                )

                val sliderColors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)

                Text(stringResource(R.string.icon_color_hue))
                Slider(
                    value = hue,
                    onValueChange = { onSlide { hue = it } },
                    valueRange = 0f..360f,
                    colors = sliderColors
                )

                Text(stringResource(R.string.icon_color_saturation))
                Slider(
                    value = saturation,
                    onValueChange = { onSlide { saturation = it } },
                    colors = sliderColors
                )

                Text(stringResource(R.string.icon_color_brightness))
                Slider(
                    value = brightness,
                    onValueChange = { onSlide { brightness = it } },
                    colors = sliderColors
                )

                OutlinedTextField(
                    value = hex,
                    onValueChange = { text ->
                        hex = text
                        parseHex(text)?.let {
                            android.graphics.Color.colorToHSV(it, hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2]
                        }
                    },
                    label = { Text(stringResource(R.string.icon_color_hex)) },
                    isError = parseHex(hex) == null,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = { onConfirm(color) }) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

/** @return The ARGB color [text] names as #RRGGBB, or null */
private fun parseHex(text: String): Int? {
    val rgb = text.trim().removePrefix("#")
    if (!RGB.matches(rgb)) return null
    return rgb.toInt(16) or 0xFF000000.toInt()
}

private val RGB = Regex("[0-9A-Fa-f]{6}")
