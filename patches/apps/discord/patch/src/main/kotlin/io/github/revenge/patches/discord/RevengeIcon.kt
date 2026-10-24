// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package io.github.revenge.patches.discord

import app.reseam.patch.patch

/** Discord's launcher icons in the Alpha build, both adaptive icons over a channel colour. */
private val LAUNCHER_ICONS = listOf("ic_logo_square_canary", "ic_logo_round_canary")

private const val BACKGROUND = "revenge_icon_background"

val revengeIcon = patch("Revenge icon") {
    description("Puts Discord's launcher icon on Revenge's red.")
    compatibleWith(DISCORD)

    execute {
        resources.addColor(BACKGROUND, "#FEB23A42")
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
