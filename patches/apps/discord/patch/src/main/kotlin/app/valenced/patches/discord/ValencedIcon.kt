// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.valenced.patches.discord

import app.reseam.patch.patch

/** Discord's launcher icons in the Stable build, both adaptive icons over a channel colour. */
private val LAUNCHER_ICONS = listOf("ic_logo_square", "ic_logo_round")

private const val BACKGROUND = "valenced_icon_background"

val valencedIcon = patch("Valenced icon") {
    description("Puts Discord's launcher icon on Valenced's cyan.")
    compatibleWith(DISCORD)

    execute {
        resources.addColor(BACKGROUND, "#FF37EBFF")
        for (icon in LAUNCHER_ICONS) {
            // The adaptive icon is the anydpi-v26 configuration; the density ones are plain PNGs.
            val adaptive = resources.paths("mipmap", icon).filter { it.endsWith(".xml") }
            check(adaptive.isNotEmpty()) { "mipmap/$icon has no adaptive icon" }
            adaptive.forEach { path ->
                files.editXml(path) {
                    val background = findByTag("background").singleOrNull() ?: error("$path has no background layer")
                    background["android:drawable"] = "@color/$BACKGROUND"
                }
            }
        }
    }
}
