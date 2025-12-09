package com.greencopper.core.metrics.provider

import com.greencopper.core.services.manager.MetricsServiceSwitch

internal class FirebaseServiceSwitch(
    firebase: FirebaseProvider,
) : MetricsServiceSwitch<FirebaseProvider>(
    firebase
)
