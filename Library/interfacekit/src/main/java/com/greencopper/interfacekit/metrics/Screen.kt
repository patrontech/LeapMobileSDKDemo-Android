package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.Screen

internal fun Screen.Companion.webview(name: String): Screen = Screen(name, "webview")
internal fun Screen.Companion.widgetCollection(name: String): Screen = Screen(name, "widget_collection")
internal fun Screen.Companion.sample(name: String): Screen = Screen(name, "sample")
internal fun Screen.Companion.mainActionCard(name: String): Screen = Screen(name, "main_action_card_onboarding_page")
internal fun Screen.Companion.adOnboarding(name: String): Screen = Screen(name, "ad_onboarding_page")
internal fun Screen.Companion.fullScreenMedia(name: String): Screen = Screen(name, "full_screen_media")
internal fun Screen.Companion.editorialPage(name: String): Screen = Screen(name, "editorial_page")
internal fun Screen.Companion.projectSwitcherScreenClass(): String = "project_switcher"
internal fun Screen.Companion.projectSwitcher(name: String): Screen = Screen(name, projectSwitcherScreenClass())
internal fun Screen.Companion.projectSwitching(name: String): Screen = Screen(name, "project_switching")
internal fun Screen.Companion.search(name: String): Screen = Screen(name, "search")
internal fun Screen.Companion.inbox(name: String): Screen = Screen(name, "inbox")
internal fun Screen.Companion.interestsPicker(name: String) = Screen(name, "interests_picker")
