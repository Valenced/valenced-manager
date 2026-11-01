package com.aliucord.manager.ui.screens.settings

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.aliucord.manager.manager.PathManager
import com.aliucord.manager.manager.PreferencesManager
import com.aliucord.manager.manager.InstallerManager
import com.aliucord.manager.ui.theme.Theme
import com.aliucord.manager.util.*
import app.valenced.manager.BuildConfig
import app.valenced.manager.R

class SettingsModel(
    private val application: Application,
    private val paths: PathManager,
    val preferences: PreferencesManager,
    private val installerManager: InstallerManager,
) : ScreenModel {
    val installInfo = InstallInfo


    var patchedApkExists by mutableStateOf(paths.patchedApk().exists())
        private set

    var showThemeDialog by mutableStateOf(false)
        private set

    val availableInstallers = installerManager.getAvailableInstallers()

    fun showThemeDialog() {
        showThemeDialog = true
    }

    fun hideThemeDialog() {
        showThemeDialog = false
    }

    fun setTheme(theme: Theme) {
        preferences.theme = theme
    }

    fun setInstaller(installer: com.aliucord.manager.manager.InstallerSetting) {
        preferences.installer = installer
    }

    fun setKeepPatchedApks(value: Boolean) {
        preferences.keepPatchedApks = value
    }

    fun clearCache() = screenModelScope.launchIO {
        paths.clearCache()

        mainThread {
            patchedApkExists = false
            application.showToast(R.string.action_cleared_cache)
        }
    }

    fun copyInstallInfo() {
        application.copyToClipboard(installInfo)
        application.showToast(R.string.action_copied)
    }

    fun shareApk(uriToShare: android.net.Uri? = null) {
        val fileUri = uriToShare ?: run {
            val file = paths.patchedApk()
            if (!file.exists()) {
                application.showToast(R.string.settings_export_apk_not_implemented)
                return
            }
            try {
                androidx.core.content.FileProvider.getUriForFile(
                    application,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    file
                )
            } catch (t: Throwable) {
                application.showToast(R.string.status_failed)
                return
            }
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
            .setType("application/vnd.android.package-archive")
            .putExtra(android.content.Intent.EXTRA_STREAM, fileUri)
            .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            application.startActivity(intent)
        } catch (t: Throwable) {
            android.util.Log.w(BuildConfig.TAG, "Failed to share APK", t)
            application.showToast(R.string.status_failed)
        }
    }

    companion object {
        @Suppress("KotlinConstantConditions")
        private val InstallInfo: String = """
            ${BuildConfig.APPLICATION_NAME}
            Version: ${BuildConfig.VERSION_NAME}
            Version Code: ${BuildConfig.VERSION_CODE}
            Release: ${if (BuildConfig.RELEASE) "Yes" else "No"}
            Git Branch: ${BuildConfig.GIT_BRANCH}
            Git Commit: ${BuildConfig.GIT_COMMIT}
            Git Changes: ${if (BuildConfig.GIT_LOCAL_CHANGES) "Yes" else "No"}
        """.trimIndent()
    }
}
