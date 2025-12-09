package com.greencopper.core.metrics.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.greencopper.toolkit.logging.LogLevel
import com.greencopper.toolkit.logging.multilogging.configurations.FormattedLoggingConfiguration

public class CrashlyticsLoggingConfiguration : FormattedLoggingConfiguration() {
    override fun logFormattedToDestination(priority: LogLevel, message: String, tag: String?, throwable: Throwable?) {
        FirebaseCrashlytics.getInstance().log((tag ?: "") + message)
        if(throwable != null) {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }
}