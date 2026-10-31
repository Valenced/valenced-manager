package app.valenced.manager.domain.manager

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import app.valenced.manager.BuildConfig
import app.valenced.manager.R
import app.valenced.manager.domain.manager.base.BasePreferenceManager
import java.util.concurrent.TimeUnit

class PreferenceManager(context: Context, paths: PathManager) :
    BasePreferenceManager(context.getSharedPreferences("prefs", Context.MODE_PRIVATE)) {

    var packageName by stringPreference("package_name", BuildConfig.MODDED_APP_PACKAGE_NAME)

    var appName by stringPreference("app_name", BuildConfig.MOD_NAME)

    var moduleVersion by stringPreference("module_version", "")

    // Replaces the patch_icon switch, which picked Valenced's icon or Discord's
    var launcherIcon by enumPreference(
        "launcher_icon",
        if (getBoolean("patch_icon", true)) LauncherIcon.VALENCED else LauncherIcon.DISCORD
    )

    var launcherIconColor by colorPreference("launcher_icon_color", Color(BuildConfig.MODDED_APP_ICON))

    var debuggable by booleanPreference("debuggable", false)

    var mirror by enumPreference("mirror", Mirror.DEFAULT)

    var monet by booleanPreference("monet", false)

    var isDeveloper by booleanPreference("is_developer", false)

    var autoClearCache by booleanPreference("auto_clear_cache", true)

    var theme by enumPreference("theme", Theme.SYSTEM)

    var updateDuration by enumPreference("update_duration", UpdateCheckerDuration.HOURLY)

    var moduleLocation by filePreference("module_location", paths.moduleApk)

    var patchBundleLocation by stringPreference("patch_bundle_location", "")

    var installMethod by enumPreference("install_method", InstallMethod.DEFAULT)

    var logsAlternateBackground by booleanPreference("logs_alternate_bg", true)

    var logsLineWrap by booleanPreference("logs_line_wrap", false)

    var allowDowngrade by booleanPreference("allow_downgrade", false)

    var hasAskedForBatteryOpt by booleanPreference("has_asked_for_battery_opt", false)

    init {
        if (mirror !in Mirror.entries) {
            mirror = Mirror.DEFAULT
        }
    }
}

enum class Theme(@StringRes val labelRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

enum class UpdateCheckerDuration(@StringRes val labelRes: Int, val time: Long, val unit: TimeUnit) {
    DISABLED(R.string.duration_disabled, 0, TimeUnit.SECONDS),
    QUARTERLY(R.string.duration_fifteen_min, 15, TimeUnit.MINUTES),
    HALF_HOUR(R.string.duration_half_hour, 30, TimeUnit.MINUTES),
    HOURLY(R.string.duration_hourly, 1, TimeUnit.HOURS),
    BIHOURLY(R.string.duration_bihourly, 2, TimeUnit.HOURS),
    TWICE_DAILY(R.string.duration_twice_daily, 12, TimeUnit.HOURS),
    DAILY(R.string.duration_daily, 1, TimeUnit.DAYS),
    WEEKLY(R.string.duration_weekly, 7, TimeUnit.DAYS)
}

enum class LauncherIcon(@StringRes val labelRes: Int) {
    DISCORD(R.string.launcher_icon_discord),
    VALENCED(R.string.launcher_icon_valenced),
    COLOR(R.string.launcher_icon_color),
    CUSTOM(R.string.launcher_icon_custom);

    companion object {
        /** Discord's logo on a [COLOR] background, whichever of black or white shows on it */
        fun logoColor(background: Color) = if (background.luminance() > 0.5f) Color.Black else Color.White
    }
}

enum class Mirror(val baseUrl: String) {
    DEFAULT("https://proxy.vd.ushie.dev"),
    VENDETTA("https://tracker.vendetta.rocks"),
}

enum class InstallMethod(@StringRes val labelRes: Int) {
    DEFAULT(R.string.default_installer),
    SHIZUKU(R.string.shizuku_installer)
}