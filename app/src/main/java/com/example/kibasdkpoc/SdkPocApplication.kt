package com.example.kibasdkpoc

import android.app.Application
import android.util.Log
import com.example.kibasdkpoc.analytics.MyAnalyticsProvider
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.toolkit.logging.LogLevel
import com.greencopper.leapmobilesdk.toolkit.logging.multilogging.LoggingConfiguration

public class SdkPocApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        LeapMobileSDK.initialize(
            this@SdkPocApplication,
            logging = HostLoggingConfiguration(),
            metrics = MyAnalyticsProvider()
        )
    }
}

private class HostLoggingConfiguration : LoggingConfiguration {
    override fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?
    ) {
        val t = tag ?: "LeapSDK"
        val msg = if (throwable != null) "$message\n${Log.getStackTraceString(throwable)}" else message
        Log.println(priority.value, t, msg)
    }
}
