package com.greencopper.core.metrics.provider

import android.content.Context

public interface LifecycleAwareProvider {
    public fun onActivityStart(activityContext: Context)
    public fun onActivityStop(activityContext: Context)
}