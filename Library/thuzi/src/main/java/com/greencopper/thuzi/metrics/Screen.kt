package com.greencopper.thuzi.metrics

import com.greencopper.core.metrics.Screen

public fun Screen.Companion.eventPass(name: String): Screen = Screen(name = name, klass = "thuzi_event_pass")
public fun Screen.Companion.logout(name: String): Screen = Screen(name = name, klass = "thuzi_logout")
