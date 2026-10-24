package app.valenced.manager.ui.viewmodel.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import app.valenced.manager.BuildConfig
import app.valenced.manager.R
import app.valenced.manager.domain.manager.DownloadManager
import app.valenced.manager.domain.manager.DownloadResult
import app.valenced.manager.domain.manager.InstallManager
import app.valenced.manager.domain.manager.InstallMethod
import app.valenced.manager.domain.manager.PreferenceManager
import app.valenced.manager.domain.repository.RestRepository
import app.valenced.manager.installer.Installer
import app.valenced.manager.installer.session.SessionInstaller
import app.valenced.manager.installer.shizuku.ShizukuInstaller
import app.valenced.manager.installer.shizuku.ShizukuPermissions
import app.valenced.manager.network.dto.Release
import app.valenced.manager.network.utils.CommitsPagingSource
import app.valenced.manager.network.utils.dataOrNull
import app.valenced.manager.network.utils.ifSuccessful
import app.valenced.manager.utils.DiscordVersion
import app.valenced.manager.utils.isMiui
import app.valenced.manager.utils.showToast
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    private val repo: RestRepository,
    val context: Context,
    val prefs: PreferenceManager,
    val installManager: InstallManager,
    private val downloadManager: DownloadManager
) : ScreenModel {

    private val cacheDir = context.externalCacheDir ?: File(
        Environment.getExternalStorageDirectory(),
        Environment.DIRECTORY_DOWNLOADS
    ).resolve(BuildConfig.MANAGER_NAME).also { it.mkdirs() }

    var discordVersions by mutableStateOf<Map<DiscordVersion.Type, DiscordVersion?>?>(null)
        private set

    var release by mutableStateOf<Release?>(null)
        private set

    private var updateDownloadUrl by mutableStateOf<String?>(null)
    var showUpdateDialog by mutableStateOf(false)
    var isUpdating by mutableStateOf(false)
    val commits = Pager(PagingConfig(pageSize = 30)) { CommitsPagingSource(repo) }.flow.cachedIn(
        screenModelScope
    )

    init {
        getDiscordVersions()
        checkForUpdate()
    }

    fun getDiscordVersions() {
        screenModelScope.launch {
            discordVersions = repo.getLatestDiscordVersions().dataOrNull
            if (prefs.autoClearCache) autoClearCache()
        }
    }

    fun launchMod() {
        installManager.current?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun uninstallMod() {
        installManager.uninstall()
    }

    fun launchModInfo() {
        installManager.current?.let {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse("package:${it.packageName}")
                context.startActivity(this)
            }
        }
    }

    private fun autoClearCache() {
        val currentVersion =
            DiscordVersion.fromVersionCode(installManager.current?.versionCode.toString()) ?: return
        val latestVersion = when {
            prefs.discordVersion.isBlank() -> discordVersions?.get(prefs.channel)
            else -> DiscordVersion.fromVersionCode(prefs.discordVersion)
        } ?: return

        if (latestVersion > currentVersion) {
            for (file in (context.externalCacheDir ?: context.cacheDir).listFiles()
                ?: emptyArray()) {
                if (file.isDirectory) file.deleteRecursively()
            }
        }
    }

    private fun checkForUpdate() {
        screenModelScope.launch {
            release = repo.getLatestRelease("Valenced/valenced-manager").dataOrNull
            release?.let {
                updateDownloadUrl = it.assets.firstOrNull { asset -> asset.name.endsWith(".apk") }?.browserDownloadUrl
                showUpdateDialog = it.tagName.removePrefix("v") != BuildConfig.VERSION_NAME
            }
            repo.getLatestRelease("Valenced/valenced").ifSuccessful {
                if (prefs.moduleVersion != it.tagName) {
                    prefs.moduleVersion = it.tagName
                    val module = File(cacheDir, "xposed.apk")
                    if (module.exists()) module.delete()
                }
            }
        }
    }

    fun downloadAndInstallUpdate(onProgressUpdate: (Float?) -> Unit) {
        screenModelScope.launch {
            val update = File(cacheDir, "update.apk")
            if (update.exists()) update.delete()
            isUpdating = true
            val downloadResult =
                downloadManager.downloadUpdate(updateDownloadUrl!!, update, onProgressUpdate)
            isUpdating = false

            if (downloadResult !is DownloadResult.Success) {
                if (downloadResult is DownloadResult.Error) {
                    Log.e("HomeViewModel", "Download failed: ${downloadResult.debugReason}")
                    context.showToast(R.string.msg_download_failed)
                }
                return@launch
            }

            val installMethod = if (prefs.installMethod == InstallMethod.SHIZUKU && !ShizukuPermissions.waitShizukuPermissions()) {
                // Temporarily use DEFAULT if SHIZUKU permissions are not granted
                    context.showToast(R.string.msg_shizuku_denied)
                    InstallMethod.DEFAULT
                } else {
                    prefs.installMethod
                }

            val installer: Installer = when (installMethod) {
                InstallMethod.DEFAULT -> SessionInstaller(context)
                InstallMethod.SHIZUKU -> ShizukuInstaller(context)
            }

            installer.installApks(silent = !isMiui, update)
        }
    }

}