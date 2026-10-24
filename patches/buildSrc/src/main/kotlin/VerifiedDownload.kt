// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

import java.net.URI
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.HexFormat
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Downloads [url] to [output] and fails unless its SHA-256 is [sha256]. */
abstract class VerifiedDownload : DefaultTask() {
    @get:Input abstract val url: Property<String>

    @get:Input abstract val sha256: Property<String>

    @get:OutputFile abstract val output: RegularFileProperty

    @TaskAction
    fun download() {
        val target = output.get().asFile
        val digest = MessageDigest.getInstance("SHA-256")
        URI(url.get()).toURL().openStream().use { input ->
            DigestInputStream(input, digest).use { hashed ->
                target.outputStream().use { hashed.copyTo(it) }
            }
        }
        val actual = HexFormat.of().formatHex(digest.digest())
        if (actual != sha256.get()) {
            target.delete()
            throw GradleException("${url.get()} has SHA-256 $actual, expected ${sha256.get()}")
        }
    }
}
