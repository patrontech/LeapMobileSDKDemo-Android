package com.greencopper.core.metrics.provider

import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.UserProperty

public interface MappedProvider : MetricsProvider {

    /** Takes a tracking event and applies the provider
     * transformations and restrictions before firing it to the appropriate system.
     */
    public fun track(event: EventName, parameters: Map<EventParameter, String>)

    /** Tracks user properties if the tracking system used supports them. */
    public fun track(parameters: Map<UserProperty, String>)
}