package com.aliucord.manager.installers

import android.os.Parcelable

interface InstallerError : Parcelable {
    val message: String
}
