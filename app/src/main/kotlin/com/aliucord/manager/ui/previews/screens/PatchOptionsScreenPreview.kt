package com.aliucord.manager.ui.previews.screens

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.aliucord.manager.ui.screens.patchopts.PackageNameState
import com.aliucord.manager.ui.screens.patchopts.PatchOptions
import com.aliucord.manager.ui.screens.patchopts.PatchOptionsScreenContent
import com.aliucord.manager.ui.screens.patchopts.VersionPreference
import com.aliucord.manager.ui.theme.ManagerTheme

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
private fun PatchOptionsScreenPreview(
    @PreviewParameter(PatchOptionsParametersProvider::class)
    parameters: PatchOptionsParameters,
) {
    ManagerTheme {
        PatchOptionsScreenContent(
            isUpdate = parameters.isUpdate,
            isDevMode = parameters.isDevMode,
            debuggable = parameters.debuggable,
            setDebuggable = {},
            oldLogo = parameters.oldLogo,
            selectedColor = parameters.selectedColor,
            selectedImage = { parameters.selectedImage },
            onOpenIconOptions = {},
            appName = parameters.appName,
            appNameIsError = parameters.appNameIsError,
            setAppName = {},
            packageName = parameters.packageName,
            packageNameState = parameters.packageNameState,
            setPackageName = {},
            versionPreference = parameters.versionPreference,
            setVersionPreference = {},
            customVersionCode = parameters.customVersionCode,
            customVersionCodeIsError = parameters.customVersionCodeIsError,
            setCustomVersionCode = {},
            isConfigValid = parameters.isConfigValid,
            onInstall = {},
        )
    }
}

private data class PatchOptionsParameters(
    val isUpdate: Boolean,
    val isDevMode: Boolean,
    val debuggable: Boolean,
    val oldLogo: Boolean,
    val selectedColor: Color?,
    val selectedImage: ByteArray?,
    val appName: String,
    val appNameIsError: Boolean,
    val packageName: String,
    val packageNameState: PackageNameState,
    val versionPreference: VersionPreference,
    val customVersionCode: String,
    val customVersionCodeIsError: Boolean,
    val isConfigValid: Boolean,
)

private class PatchOptionsParametersProvider : PreviewParameterProvider<PatchOptionsParameters> {
    override val values = sequenceOf(
        // Default initial install
        PatchOptionsParameters(
            isUpdate = false,
            isDevMode = false,
            debuggable = false,
            oldLogo = false,
            selectedColor = PatchOptions.IconReplacement.ValencedColor,
            selectedImage = null,
            appName = PatchOptions.Default.appName,
            appNameIsError = false,
            packageName = PatchOptions.Default.packageName,
            packageNameState = PackageNameState.Ok,
            versionPreference = VersionPreference.Alpha,
            customVersionCode = "352206",
            customVersionCodeIsError = false,
            isConfigValid = true,
        ),
    )
}
