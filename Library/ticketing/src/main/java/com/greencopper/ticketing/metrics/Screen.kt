package com.greencopper.ticketing.metrics

import com.greencopper.core.metrics.Screen

internal fun Screen.Companion.ticketsScan(name: String): Screen = Screen(name = name, klass = "ticketing_tickets_scan")
internal fun Screen.Companion.showclixLogin(name: String): Screen = Screen(name = name, klass = "ticketing_showclix_login")
