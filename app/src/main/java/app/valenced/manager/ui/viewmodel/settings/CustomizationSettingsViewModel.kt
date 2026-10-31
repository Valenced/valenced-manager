package app.valenced.manager.ui.viewmodel.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.valenced.manager.R
import app.valenced.manager.domain.manager.LauncherIcon
import app.valenced.manager.domain.manager.PathManager
import app.valenced.manager.domain.manager.PreferenceManager
import app.valenced.manager.utils.showToast
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomizationSettingsViewModel(
    private val context: Context,
    private val prefs: PreferenceManager,
    private val paths: PathManager,
) : ScreenModel {

    /** Changes each time a new custom icon is written, to reload its preview. */
    var customIconVersion by mutableIntStateOf(0)
        private set

    var hasCustomIcon by mutableStateOf(paths.customIcon.isFile)
        private set

    fun setCustomIcon(uri: Uri) = screenModelScope.launch {
        runCatching { withContext(Dispatchers.IO) { writeCustomIcon(uri) } }
            .onSuccess {
                customIconVersion++
                hasCustomIcon = true
                prefs.launcherIcon = LauncherIcon.CUSTOM
            }
            .onFailure {
                Log.e(TAG, "Couldn't use $uri as the app icon", it)
                context.showToast(R.string.msg_custom_icon_failed)
            }
    }

    /** Crops the image to a square and centers it in an adaptive icon layer, where launchers show it. */
    private fun writeCustomIcon(uri: Uri) {
        val source = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            // Software, to draw it on a Canvas
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val side = minOf(info.size.width, info.size.height)
            if (side > VISIBLE_SIZE) decoder.setTargetSampleSize(side / VISIBLE_SIZE)
        }
        val side = minOf(source.width, source.height)
        val left = (source.width - side) / 2
        val top = (source.height - side) / 2
        val inset = (LAYER_SIZE - VISIBLE_SIZE) / 2

        val icon = Bitmap.createBitmap(LAYER_SIZE, LAYER_SIZE, Bitmap.Config.ARGB_8888)
        Canvas(icon).drawBitmap(
            source,
            Rect(left, top, left + side, top + side),
            Rect(inset, inset, LAYER_SIZE - inset, LAYER_SIZE - inset),
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        paths.customIcon.outputStream().use { icon.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private companion object {
        const val TAG = "CustomizationSettings"

        // An adaptive icon layer is 108dp, of which launchers show the middle 72dp. In pixels at xxxhdpi.
        const val LAYER_SIZE = 432
        const val VISIBLE_SIZE = 288
    }

}
