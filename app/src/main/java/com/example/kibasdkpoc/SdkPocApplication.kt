package com.example.kibasdkpoc

import android.app.Application
import com.example.kibasdkpoc.analytics.MyAnalyticsProvider
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.toolkit.logging.multilogging.configurations.ConsoleLoggingConfiguration

public class SdkPocApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        LeapMobileSDK.initialize(
            this@SdkPocApplication,
            logging = ConsoleLoggingConfiguration(),
            metrics = MyAnalyticsProvider()
        )
    }
}
