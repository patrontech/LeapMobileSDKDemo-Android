package com.greencopper.toolkit.extensions

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

@SuppressLint("NewApi")
@Suppress("Deprecation", "UNCHECKED_CAST")
public fun <T : java.io.Serializable> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T? {
    val sdkVersion = App.resolve<BuildConfigProvider>().sdkInt
    return if (sdkVersion < Build.VERSION_CODES.TIRAMISU) {
        getSerializable(key) as? T
    } else {
        getSerializable(key, clazz)
    }
}
