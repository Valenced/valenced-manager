package com.aliucord.manager.ui.screens.patchopts

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.aliucord.manager.util.serialization.ColorParceler
import com.aliucord.manager.util.serialization.ColorSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class VersionPreference {
    Stable,
    Beta,
    Alpha,
    Custom
}

@Immutable
@Parcelize
@Serializable
data class PatchOptions(
        /** The app name that's user-facing in launchers. */
        val appName: String,

        /** Changes the installation package name. */
        val packageName: String,

        /** Adding the debuggable APK flag. */
        val debuggable: Boolean,

        /** Replacement of the user-facing launcher icon. */
        val iconReplacement: IconReplacement,

        /** Version preference to fetch and install */
        val versionPreference: VersionPreference,

        /**
         * Version to fetch and install when versionPreference is set to VersionPreference.Custom.
         */
        val customVersionCode: String,
) : Parcelable {
    @Immutable
    @Parcelize
    @Serializable
    sealed interface IconReplacement : Parcelable {
        /** Valenced Cyan background (#37EBFF) */
        @Immutable
        @Parcelize
        @Serializable
        @SerialName("valenced")
        data object Valenced : IconReplacement

        /**
         * Changes the background of the icon to a specific color without altering the foreground or
         * monochrome variants.
         */
        @Immutable
        @Parcelize
        @Serializable
        @SerialName("color")
        data class CustomColor(
                @TypeParceler<Color, ColorParceler>
                @Serializable(ColorSerializer::class)
                val color: Color,
        ) : IconReplacement

        /**
         * Replaces the foreground image of the icon entirely and sets the background to
         * transparent. This does not affect the monochrome icon.
         */
        @Immutable
        @Parcelize
        @Serializable
        @SerialName("image")
        data class CustomImage(val imageBytes: ByteArray) : IconReplacement {
            override fun hashCode() = imageBytes.contentHashCode()
            override fun equals(other: Any?) =
                    this === other ||
                            (javaClass == other?.javaClass &&
                                    imageBytes.contentEquals((other as CustomImage).imageBytes))
        }

        companion object {
            /** The default Valenced background color. */
            val ValencedColor = Color(0xFF37EBFF)
        }
    }

    companion object {
        val Default =
                PatchOptions(
                        appName = "Valenced",
                        packageName = "app.valenced",
                        debuggable = false,
                        iconReplacement = IconReplacement.Valenced,
                        versionPreference = VersionPreference.Alpha,
                        customVersionCode = "352206",
                )
    }
}
