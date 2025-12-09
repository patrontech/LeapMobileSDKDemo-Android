package com.greencopper.event.recipe

import com.greencopper.core.automation.AutomationInfo
import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val NO_REMINDERS_INTERVAL = -1

@Serializable
internal data class EventConfiguration(
    val reminders: Reminders? = null,
    val myActivities: EventFeatureInfo? = null,
    val mySchedule: EventFeatureInfo? = null,
) : KiboSerializable<EventConfiguration> {

    @Serializable
    internal data class Reminders(
        val topBarIcon: String,
        val timeIntervals: List<TimeInterval>,
        val defaultTimeInterval: Int, // -1 means no reminders
        @SerialName("onFirstAddToMySchedule") val onFirstAddToMyScheduleRouteLink: String,
        @SerialName("onNotificationTap") val onNotificationTapRouteLink: String,
    )

    @Serializable
    internal data class TimeInterval(
        val label: String,
        val value: Int, // in minutes
        val notificationMessage: String? = null,
    )

    @Serializable
    internal data class EventFeatureInfo(
        val automations: List<AutomationInfo>,
    )

    override fun getSerializer(): KSerializer<EventConfiguration> = serializer()
}
