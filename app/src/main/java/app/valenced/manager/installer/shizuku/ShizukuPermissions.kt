package app.valenced.manager.installer.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import kotlin.coroutines.resume

object ShizukuPermissions {

    private const val REQUEST_CODE = 1

    suspend fun waitShizukuPermissions(): Boolean {
        if (!Shizuku.pingBinder()) return false
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) return true

        return suspendCancellableCoroutine { continuation ->
            lateinit var listener: OnRequestPermissionResultListener
            listener = OnRequestPermissionResultListener { requestCode, grantResult ->
                if (requestCode != REQUEST_CODE) return@OnRequestPermissionResultListener

                Shizuku.removeRequestPermissionResultListener(listener)
                continuation.resume(grantResult == PackageManager.PERMISSION_GRANTED)
            }

            Shizuku.addRequestPermissionResultListener(listener)
            continuation.invokeOnCancellation {
                Shizuku.removeRequestPermissionResultListener(listener)
            }
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

}
