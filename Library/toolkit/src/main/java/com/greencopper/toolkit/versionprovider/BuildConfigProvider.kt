package com.greencopper.toolkit.versionprovider

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.greencopper.toolkit.BuildConfig

public interface BuildConfigProvider {
    public val sdkInt: Int
    public val versionName: String
    public val versionCode: Int
    public val manufacturer: String
    public val model: String
    public val isDebug: Boolean
    public val libraryVersion: String
}

public class ConcreteBuildConfigProvider(
    private val context: Context,
) : BuildConfigProvider {

    private val packageInfo get() = context.packageManager.getPackageInfo(context.packageName, 0)

    override val sdkInt: Int = Build.VERSION.SDK_INT
    override val versionName: String = packageInfo.versionName ?: "unspecified"
    override val versionCode: Int =
        PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
    override val manufacturer: String = Build.MANUFACTURER
    override val model: String = Build.MODEL
    override val isDebug: Boolean  = BuildConfig.DEBUG
    override val libraryVersion: String = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData?.getString("com.leapevent.library_version") ?: "unavailable"
}
