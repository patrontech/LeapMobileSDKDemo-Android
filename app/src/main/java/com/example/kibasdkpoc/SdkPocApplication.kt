package com.example.kibasdkpoc

import android.app.Application
import com.greencopper.toolkit.logging.multilogging.configurations.ConsoleLoggingConfiguration

public class SdkPocApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LeapMobileSDK.initialize(this@SdkPocApplication, ConsoleLoggingConfiguration())
    }
}
