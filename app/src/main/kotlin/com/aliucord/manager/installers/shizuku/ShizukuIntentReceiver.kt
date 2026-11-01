package com.aliucord.manager.installers.shizuku

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.util.Log
import com.aliucord.manager.installers.shizuku.ShizukuInstallerError
import com.aliucord.manager.installers.InstallerResult
import app.valenced.manager.BuildConfig

class ShizukuIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras ?: return
        val status = extras.getInt(PackageInstaller.EXTRA_STATUS)
        val message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "Unknown"
        val sessionId = extras.getInt(EXTRA_SESSION_ID)

        if (BuildConfig.DEBUG) {
            Log.d(
                BuildConfig.TAG,
                "Received install result for session $sessionId: " +
                        "status=$status, message=$message"
            )
        }

        val result = when (status) {
            PackageInstaller.STATUS_SUCCESS -> InstallerResult.Success
            else -> ShizukuInstallerError("status=$status, message=$message")
        }

        if (extras.getBoolean(EXTRA_RELAY_ENABLED)) {
            val resultIntent = Intent(context, ShizukuResultReceiver::class.java).apply {
                putExtras(Bundle().apply {
                    putInt(EXTRA_SESSION_ID, sessionId)
                    putParcelable(EXTRA_RESULT, result)
                })
            }

            context.sendBroadcast(resultIntent)
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "com.aliucord.manager.intent.extra.SESSION_ID"
        const val EXTRA_RELAY_ENABLED = "com.aliucord.manager.intent.extra.RELAY_ENABLED"
        const val EXTRA_RESULT = "com.aliucord.manager.intent.extra.RESULT"
    }
}
