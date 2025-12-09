package com.greencopper.event.metrics

import com.greencopper.core.metrics.Screen

internal fun Screen.Companion.activitiesList(name: String): Screen =
    Screen(name = name, klass = activitiesList_class)

internal const val activitiesList_class = "activities_list"

internal fun Screen.Companion.activityDetail(name: String): Screen =
    Screen(name = name, klass = "activity_detail")

internal fun Screen.Companion.scheduleItemDetail(name: String): Screen =
    Screen(name = name, klass = "schedule_detail")

public fun Screen.Companion.schedule(name: String): Screen =
    Screen(name = name, klass = "schedule")

internal fun Screen.Companion.scheduleReminders(name: String): Screen =
    Screen(name = name, klass = "schedule_reminders_selector")

internal fun Screen.Companion.performersList(name: String): Screen =
    Screen(name = name, klass = performersList_class)

internal const val performersList_class = "performers_list"

internal fun Screen.Companion.performerDetail(name: String): Screen =
    Screen(name = name, klass = "performer_detail")
