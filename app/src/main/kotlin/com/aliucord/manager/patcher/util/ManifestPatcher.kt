/*
 * Copyright (c) 2022 Juby210 & zt
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.manager.patcher.util

import android.Manifest
import android.os.Build
import pxb.android.axml.*

object ManifestPatcher {
    private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    private const val DEBUGGABLE = "debuggable"
    private const val VM_SAFE_MODE = "vmSafeMode"
    private const val USE_EMBEDDED_DEX = "useEmbeddedDex"
    private const val EXTRACT_NATIVE_LIBS = "extractNativeLibs"
    private const val REQUEST_LEGACY_EXTERNAL_STORAGE = "requestLegacyExternalStorage"
    private const val NETWORK_SECURITY_CONFIG = "networkSecurityConfig"
    private const val LABEL = "label"
    private const val PACKAGE = "package"
    private const val COMPILE_SDK_VERSION = "compileSdkVersion"
    private const val COMPILE_SDK_VERSION_CODENAME = "compileSdkVersionCodename"

    fun patchManifest(
        manifestBytes: ByteArray,
        packageName: String? = null,
        appName: String? = null,
        debuggable: Boolean? = null,
        compileSdkVersion: Int? = 23,
        compileSdkVersionCodename: String? = "6.0-2438415",
        addManageExternalStorage: Boolean? = true,
        targetSdkVersion: Int? = if (Build.VERSION.SDK_INT >= 31) 30 else 28,
        requestLegacyExternalStorage: Boolean? = true,
        vmSafeMode: Boolean? = true,
        useEmbeddedDex: Boolean? = true,
        extractNativeLibs: Boolean? = false,
        addManagerMetadata: Boolean? = true,
        modifyPermissions: Boolean? = true,
        modifyProviders: Boolean? = true,
        modifyActivities: Boolean? = true,
    ): ByteArray {
        val reader = AxmlReader(manifestBytes)
        val writer = AxmlWriter()

        reader.accept(
            object : AxmlVisitor(writer) {
                override fun ns(prefix: String?, uri: String?, ln: Int) {
                    val realUri = uri ?: ANDROID_NAMESPACE
                    super.ns(prefix, realUri, ln)
                }

                override fun child(ns: String?, name: String?) =
                    object :
                        ReplaceAttrsVisitor(
                            super.child(ns, name),
                            buildMap {
                                packageName?.let { put(PACKAGE, it) }
                                compileSdkVersion?.let {
                                    put(COMPILE_SDK_VERSION, it)
                                }
                                compileSdkVersionCodename?.let {
                                    put(COMPILE_SDK_VERSION_CODENAME, it)
                                }
                            }
                        ) {
                        private var shouldAddExternalStoragePerm = false
                        private val shouldAddUsesForDeclaredPermissions = mutableListOf<String>()

                        override fun child(ns: String?, name: String): NodeVisitor {
                            val nv = super.child(ns, name)

                            // Add MANAGE_EXTERNAL_STORAGE when necessary
                            if (shouldAddExternalStoragePerm &&
                                addManageExternalStorage == true
                            ) {
                                super.child(null, "uses-permission")
                                    .attr(
                                        ANDROID_NAMESPACE,
                                        "name",
                                        android.R.attr.name,
                                        TYPE_STRING,
                                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                                    )
                                shouldAddExternalStoragePerm = false
                            }

                            // If any declared permissions were detected that should also be
                            // exposed as <uses-permission/>, add them here (keeps declaration + usage).
                            if (shouldAddUsesForDeclaredPermissions.isNotEmpty()) {
                                for (perm in shouldAddUsesForDeclaredPermissions) {
                                    super.child(null, "uses-permission")
                                        .attr(
                                            ANDROID_NAMESPACE,
                                            "name",
                                            android.R.attr.name,
                                            TYPE_STRING,
                                            perm
                                        )
                                }
                                shouldAddUsesForDeclaredPermissions.clear()
                            }

                            return when (name) {
                                "uses-permission" ->
                                    if (modifyPermissions != false) {
                                        object : NodeVisitor(nv) {
                                            override fun attr(
                                                ns: String?,
                                                name: String?,
                                                resourceId: Int,
                                                type: Int,
                                                value: Any?
                                            ) {
                                                if (name != "maxSdkVersion") {
                                                    super.attr(
                                                        ns,
                                                        name,
                                                        resourceId,
                                                        type,
                                                        value
                                                    )
                                                }

                                                // Set the add external storage
                                                // permission to be added after
                                                // WRITE_EXTERNAL_STORAGE (which is
                                                // after read)
                                                if (name == "name" &&
                                                    value ==
                                                    Manifest.permission
                                                        .READ_EXTERNAL_STORAGE
                                                ) {
                                                    shouldAddExternalStoragePerm = true
                                                }
                                            }
                                        }
                                    } else nv

                                "uses-sdk" ->
                                    if (targetSdkVersion != null) {
                                        object : NodeVisitor(nv) {
                                            override fun attr(
                                                ns: String?,
                                                name: String?,
                                                resourceId: Int,
                                                type: Int,
                                                value: Any?
                                            ) {
                                                if (name == "targetSdkVersion") {
                                                    super.attr(
                                                        ns,
                                                        name,
                                                        resourceId,
                                                        type,
                                                        targetSdkVersion
                                                    )
                                                } else {
                                                    super.attr(
                                                        ns,
                                                        name,
                                                        resourceId,
                                                        type,
                                                        value
                                                    )
                                                }
                                            }
                                        }
                                    } else nv

                                "permission" ->
                                    if (modifyPermissions != false &&
                                        packageName != null
                                    ) {
                                        object : NodeVisitor(nv) {
                                            override fun attr(
                                                ns: String?,
                                                name: String,
                                                resourceId: Int,
                                                type: Int,
                                                value: Any?
                                            ) {
                                                val newVal = when (name) {
                                                    "name" ->
                                                        (value as String)
                                                            .replace(
                                                                "com.discord",
                                                                packageName
                                                            )

                                                    else -> value
                                                }

                                                super.attr(
                                                    ns,
                                                    name,
                                                    resourceId,
                                                    type,
                                                    newVal
                                                )

                                                // If this permission looks like a runtime/receiver permission
                                                // or is a custom declared permission, also add a corresponding
                                                // <uses-permission/> entry so other apps / the system can request it.
                                                if (name == "name" && newVal is String) {
                                                    if (newVal.endsWith("_PERMISSION") || newVal.contains("DYNAMIC_RECEIVER")) {
                                                        shouldAddUsesForDeclaredPermissions.add(newVal)
                                                    }
                                                }
                                            }
                                        }
                                    } else nv

                                "application" ->
                                    object :
                                        ReplaceAttrsVisitor(
                                            nv,
                                            buildMap {
                                                appName?.let { put(LABEL, it) }
                                                debuggable?.let {
                                                    put(DEBUGGABLE, it)
                                                }
                                                requestLegacyExternalStorage
                                                    ?.let {
                                                        put(
                                                            REQUEST_LEGACY_EXTERNAL_STORAGE,
                                                            it
                                                        )
                                                    }
                                                vmSafeMode?.let {
                                                    put(VM_SAFE_MODE, it)
                                                }
                                                useEmbeddedDex?.let {
                                                    put(USE_EMBEDDED_DEX, it)
                                                }
                                                extractNativeLibs?.let {
                                                    put(
                                                        EXTRACT_NATIVE_LIBS,
                                                        !it
                                                    )
                                                } // Inverted since we set to
                                                // false originally
                                            }
                                        ) {
                                        private var shouldAddDebuggable =
                                            debuggable ?: false
                                        private var shouldAddLegacyStorage =
                                            requestLegacyExternalStorage ?: false
                                        private var shouldAddUseEmbeddedDex =
                                            useEmbeddedDex ?: false
                                        private var shouldAddExtractNativeLibs =
                                            extractNativeLibs ?: false
                                        private var shouldAddMetadata =
                                            addManagerMetadata ?: false
                                        private var shouldAddCleartext = true


                                        override fun attr(
                                            ns: String?,
                                            name: String,
                                            resourceId: Int,
                                            type: Int,
                                            value: Any?
                                        ) {
                                            if (name == NETWORK_SECURITY_CONFIG) return
                                            if (name == REQUEST_LEGACY_EXTERNAL_STORAGE)
                                                shouldAddLegacyStorage = false
                                            if (name == USE_EMBEDDED_DEX)
                                                shouldAddUseEmbeddedDex = false
                                            if (name == EXTRACT_NATIVE_LIBS)
                                                shouldAddExtractNativeLibs = false
                                            if (name == DEBUGGABLE)
                                                shouldAddDebuggable = false
                                            if (name == "usesCleartextTraffic") {
                                                shouldAddCleartext = false
                                                super.attr(ns, name, 0x010104ec, TYPE_INT_BOOLEAN, 1)
                                                return
                                            }

                                            super.attr(
                                                ns,
                                                name,
                                                resourceId,
                                                type,
                                                value
                                            )
                                        }

                                        override fun child(
                                            ns: String?,
                                            name: String
                                        ): NodeVisitor {
                                            val visitor = super.child(ns, name)

                                            if (shouldAddMetadata &&
                                                addManagerMetadata == true
                                            ) {
                                                shouldAddMetadata = false
                                                super.child(
                                                    ANDROID_NAMESPACE,
                                                    "meta-data"
                                                )
                                                    .apply {
                                                        attr(
                                                            ANDROID_NAMESPACE,
                                                            "name",
                                                            android.R.attr.name,
                                                            TYPE_STRING,
                                                            "isValenced"
                                                        )
                                                        attr(
                                                            ANDROID_NAMESPACE,
                                                            "value",
                                                            android.R
                                                                .attr
                                                                .value,
                                                            TYPE_INT_BOOLEAN,
                                                            1
                                                        )
                                                    }
                                            }

                                            return when (name) {
                                                "activity" ->
                                                    if (modifyActivities != false &&
                                                        appName != null
                                                    ) {
                                                        ReplaceAttrsVisitor(
                                                            visitor,
                                                            mapOf(
                                                                "label" to
                                                                        appName
                                                            )
                                                        )
                                                    } else visitor

                                                "provider" ->
                                                    if (modifyProviders != false &&
                                                        packageName !=
                                                        null
                                                    ) {
                                                        object :
                                                            NodeVisitor(
                                                                visitor
                                                            ) {
                                                            override fun attr(
                                                                ns: String?,
                                                                name: String,
                                                                resourceId: Int,
                                                                type: Int,
                                                                value: Any?
                                                            ) {
                                                                super.attr(
                                                                    ns,
                                                                    name,
                                                                    resourceId,
                                                                    type,
                                                                    if (name ==
                                                                        "authorities"
                                                                    ) {
                                                                        (value as
                                                                                String)
                                                                            .replace(
                                                                                "com.discord",
                                                                                packageName
                                                                            )
                                                                    } else {
                                                                        value
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    } else visitor

                                                else -> visitor
                                            }
                                        }

                                        override fun end() {
                                            if (shouldAddLegacyStorage &&
                                                Build.VERSION.SDK_INT >=
                                                29 &&
                                                requestLegacyExternalStorage ==
                                                true
                                            ) {
                                                super.attr(
                                                    ANDROID_NAMESPACE,
                                                    REQUEST_LEGACY_EXTERNAL_STORAGE,
                                                    android.R
                                                        .attr
                                                        .requestLegacyExternalStorage,
                                                    TYPE_INT_BOOLEAN,
                                                    1
                                                )
                                            }
                                            if (shouldAddDebuggable &&
                                                debuggable == true
                                            ) {
                                                super.attr(
                                                    ANDROID_NAMESPACE,
                                                    DEBUGGABLE,
                                                    android.R.attr.debuggable,
                                                    TYPE_INT_BOOLEAN,
                                                    1
                                                )
                                            }

                                            // Disable AOT (Necessary for AOSP Android
                                            // 15)
                                            if (Build.VERSION.SDK_INT >= 29 &&
                                                shouldAddUseEmbeddedDex &&
                                                useEmbeddedDex == true
                                            ) {
                                                super.attr(
                                                    ANDROID_NAMESPACE,
                                                    USE_EMBEDDED_DEX,
                                                    android.R.attr.useEmbeddedDex,
                                                    TYPE_INT_BOOLEAN,
                                                    1
                                                )
                                            }

                                            if (shouldAddExtractNativeLibs &&
                                                extractNativeLibs == false
                                            ) {
                                                super.attr(
                                                    ANDROID_NAMESPACE,
                                                    EXTRACT_NATIVE_LIBS,
                                                    android.R
                                                        .attr
                                                        .extractNativeLibs,
                                                    TYPE_INT_BOOLEAN,
                                                    0
                                                )
                                            }
                                            if (shouldAddCleartext) {
                                                super.attr(
                                                    ANDROID_NAMESPACE,
                                                    "usesCleartextTraffic",
                                                    0x010104ec,
                                                    TYPE_INT_BOOLEAN,
                                                    1
                                                )
                                            }
                                            super.end()
                                        }
                                    }

                                else -> nv
                            }
                        }
                    }
            }
        )

        return writer.toByteArray()
    }

    private open class ReplaceAttrsVisitor(
        nv: NodeVisitor,
        private val attrs: Map<String, Any>,
    ) : NodeVisitor(nv) {
        override fun attr(ns: String?, name: String, resourceId: Int, type: Int, value: Any?) {
            val replace = attrs.containsKey(name)
            val newValue = attrs[name]

            super.attr(
                ns,
                name,
                resourceId,
                if (newValue is String) TYPE_STRING else type,
                if (replace) newValue else value
            )
        }
    }
}
