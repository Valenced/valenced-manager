package com.aliucord.manager.installers.shizuku

import android.content.Context
import android.os.Parcelable
import com.aliucord.manager.installers.InstallerResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShizukuInstallerError(
    private val reason: String
) : InstallerResult.Error(), Parcelable {
    override fun getDebugReason(): String = reason
    override fun getLocalizedReason(context: Context): String? = null
}
