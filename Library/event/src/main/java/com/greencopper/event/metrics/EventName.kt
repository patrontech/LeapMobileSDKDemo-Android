package com.greencopper.event.metrics

import com.greencopper.core.metrics.labels.EventName

internal fun EventName.Companion.addToMySchedule(): EventName = EventName("my_schedule/add")
internal fun EventName.Companion.removeFromMySchedule(): EventName = EventName("my_schedule/remove")

internal fun EventName.Companion.addToMyActivities(): EventName = EventName(addToMyActivities_name)
internal const val addToMyActivities_name = "my_activities/add"

internal fun EventName.Companion.removeFromMyActivities(): EventName = EventName(removeFromMyActivities_name)
internal const val removeFromMyActivities_name = "my_activities/remove"

internal fun EventName.Companion.addToMyPerformers(): EventName = EventName(addToMyPerformers_name)
internal const val addToMyPerformers_name = "my_performers/add"

internal fun EventName.Companion.removeFromMyPerformers(): EventName = EventName(removeFromMyPerformers_name)
internal const val removeFromMyPerformers_name = "my_performers/remove"
