// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package io.github.revenge.patches.discord

import app.reseam.patch.PatchLogger
import app.reseam.patch.patch
import java.io.File
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import org.lsposed.patch.ApkPatcher
import org.lsposed.patch.KeystoreSpec
import org.lsposed.patch.PatchSpec
import org.lsposed.patch.util.Logger

val revenge = patch("Revenge") {
    description("Loads Revenge and its plugins into Discord.")
    compatibleWith(DISCORD)

    val moduleOption = pathOption(
        "module",
        title = "Revenge Xposed module",
        description = "The Revenge Xposed module APK to load.",
        required = true,
    )

    afterWrite {
        val lspatchLog = LspatchLog(log)
        val outputDir = tempFile("lspatch").apply { mkdir() }
        val loaded =
            ApkPatcher(
                    lspatchLog,
                    PatchSpec.builder()
                        .apk(output.file)
                        .module(File(options[moduleOption].path))
                        .keystore(lspatchKeystore(tempFile("lspatch.keystore")))
                        .outputDir(outputDir)
                        .build(),
                )
                .patch()
                .single()
        check(lspatchLog.errors.isEmpty()) { "LSPatch: ${lspatchLog.errors.joinToString("; ")}" }
        output.replace(loaded)
    }
}

/**
 * LSPatch always signs, and its built-in keystore is JKS, which ART cannot load. The signature is
 * replaced when the output is signed, so any key works, stored in the platform's own keystore type.
 */
private fun lspatchKeystore(file: File): KeystoreSpec {
    val key =
        KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(resource("/lspatch/signing.pk8").readBytes()))
    val certificate =
        resource("/lspatch/signing.der").openStream().use {
            CertificateFactory.getInstance("X.509").generateCertificate(it)
        }
    KeyStore.getInstance(KeyStore.getDefaultType()).run {
        load(null, null)
        setKeyEntry(KEY_ALIAS, key, KEY_PASSWORD.toCharArray(), arrayOf(certificate))
        file.outputStream().use { store(it, KEY_PASSWORD.toCharArray()) }
    }
    return KeystoreSpec.of(file, KEY_PASSWORD, KEY_ALIAS, KEY_PASSWORD)
}

private const val KEY_ALIAS = "lspatch"
private const val KEY_PASSWORD = "lspatch"

/** Forwards LSPatch's log, keeping its errors: it reports some, such as an unreadable module, and goes on. */
private class LspatchLog(private val log: PatchLogger) : Logger() {
    val errors = mutableListOf<String>()

    override fun d(msg: String) = log.debug(msg)

    override fun i(msg: String) = log.info(msg)

    override fun e(msg: String) {
        errors += msg
        log.warn(msg)
    }
}
