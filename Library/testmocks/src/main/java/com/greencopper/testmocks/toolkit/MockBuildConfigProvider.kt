package com.greencopper.testmocks.toolkit

import android.os.Build
import com.greencopper.toolkit.BuildConfig
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

public class MockBuildConfigProvider(
    public var mockSdkInt: Int = Build.VERSION.SDK_INT,
    public var mockVersionName: String = Build.VERSION.RELEASE ?: "",
    public var mockVersionCode: Int = 0,
    public var mockManufacturer: String = Build.MANUFACTURER ?: "",
    public var mockModel: String = Build.MODEL ?: "",
    public var mockIsDebug: Boolean = BuildConfig.DEBUG,
    public var mockLibraryVersion: String = "",
) : BuildConfigProvider {
    override val sdkInt: Int
        get() = mockSdkInt

    override val versionName: String
        get() = mockVersionName
    override val versionCode: Int
        get() = mockVersionCode

    override val manufacturer: String
        get() = mockManufacturer

    override val model: String
        get() = mockModel

    override val isDebug: Boolean
        get() = mockIsDebug

    override val libraryVersion: String
        get() = mockLibraryVersion
}
