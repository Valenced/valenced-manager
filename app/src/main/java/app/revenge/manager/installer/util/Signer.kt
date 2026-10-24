package app.revenge.manager.installer.util

import android.content.Context
import app.reseam.sdk.SigningKeyFiles
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The key patched apps are signed with. Android only installs an update signed with the same key,
 * so the patcher creates it on first use and it is kept.
 */
object Signer : KoinComponent {
    private val context by inject<Context>()

    val keyFiles: SigningKeyFiles by lazy {
        SigningKeyFiles(
            key = context.filesDir.resolve("signing.pk8").absolutePath,
            cert = context.filesDir.resolve("signing.der").absolutePath,
        )
    }
}
