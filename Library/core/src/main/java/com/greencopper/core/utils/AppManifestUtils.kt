package com.greencopper.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

public fun appManifestHasPermission(buildConfigProvider: BuildConfigProvider, context: Context, permission: String): Boolean =
    getAppManifestPermissions(buildConfigProvider, context).contains(permission)

@SuppressLint("NewApi")
public fun getAppManifestPermissions(buildConfigProvider: BuildConfigProvider, context: Context): List<String> {
    val packageInfo = if (buildConfigProvider.sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
        )
    } else {
        context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    }

    return try {
        packageInfo
            .requestedPermissions
            ?.toList() ?: emptyList()
    } catch (e: PackageManager.NameNotFoundException) {
        App.log.e("Error getting manifest permissions", throwable = e)
        emptyList()
    }
}
