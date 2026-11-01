package com.aliucord.manager.ui.previews.screens

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.*
import androidx.core.graphics.drawable.toBitmap
import com.aliucord.manager.ui.screens.iconopts.IconOptionsMode
import com.aliucord.manager.ui.screens.iconopts.IconOptionsScreenContent
import com.aliucord.manager.ui.screens.patchopts.PatchOptions.IconReplacement
import com.aliucord.manager.ui.theme.ManagerTheme
import app.valenced.manager.R
import dev.zt64.compose.pipette.HsvColor
import java.io.ByteArrayOutputStream

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
private fun IconOptionsScreenPreview(
    @PreviewParameter(IconOptionsParametersProvider::class)
    parameters: IconOptionsParameters,
) {
    var mode by remember { mutableStateOf(parameters.mode) }
    var color by remember { mutableStateOf(HsvColor(parameters.selectedColor)) }

    DisposableEffect(parameters) {
        mode = parameters.mode
        color = HsvColor(parameters.selectedColor)

        onDispose {}
    }

    val image = parameters.selectedImage()
    ManagerTheme {
        IconOptionsScreenContent(
            mode = mode,
            setMode = { mode = it },
            selectedColor = color,
            setSelectedColor = { color = it },
            selectedImage = { image },
            setSelectedImage = {},
            onBackPressed = {},
        )
    }
}

private data class IconOptionsParameters(
    val mode: IconOptionsMode,
    val selectedColor: Color,
    val selectedImage: @Composable () -> ByteArray?,
)

private class IconOptionsParametersProvider : PreviewParameterProvider<IconOptionsParameters> {
    override val values = sequenceOf(
        IconOptionsParameters(
            mode = IconOptionsMode.Valenced,
            selectedColor = IconReplacement.ValencedColor,
            selectedImage = { null },
        ),
        IconOptionsParameters(
            mode = IconOptionsMode.CustomColor,
            selectedColor = Color(0xFF37EBFF),
            selectedImage = { null },
        ),
        IconOptionsParameters(
            mode = IconOptionsMode.CustomImage,
            selectedColor = Color.Black,
            selectedImage = { null },
        ),
    )
}
