// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.valenced.patches.discord

import java.net.URL

private object Files

internal fun resource(path: String): URL =
    Files::class.java.getResource(path) ?: error("$path is missing from the bundle's patch jar")
