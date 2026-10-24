// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package io.github.revenge.patches.discord

import app.reseam.patch.patch

val changeAppName = patch("Change app name") {
    description("Shows Discord under another name in the launcher and in Android's settings.")
    compatibleWith(DISCORD)

    val appNameOption = stringOption(
        "appName",
        title = "App name",
        description = "The name the launcher shows.",
        default = "Revenge",
    )

    execute {
        val name = options[appNameOption]
        manifest.edit {
            findByTag("application").single()["android:label"] = name
            // An activity's own label wins over the app's, and Discord's launcher activity has one.
            findByTag("activity").filter { it["android:label"] != null }.forEach { it["android:label"] = name }
        }
    }
}
