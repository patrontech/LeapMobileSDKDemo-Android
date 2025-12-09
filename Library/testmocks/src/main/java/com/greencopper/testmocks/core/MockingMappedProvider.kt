package com.greencopper.testmocks.core

import android.content.Context
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.*

public val Provider.Companion.testProvider get() = Provider("testProvider")

public open class MockingMappedProvider(
    override val name: Provider = Provider.testProvider
) : MappedProvider {

    private val savedEventList = mutableMapOf<EventName, Map<EventParameter, String>>()
    private val savedPropertyList = mutableMapOf<UserProperty, String>()
    private var enabled: Boolean = false

    override fun track(event: EventName, parameters: Map<EventParameter, String>) {
        if (!enabled) return

        // Saves the event that got tracked
        savedEventList[event] = parameters
    }

    override fun track(parameters: Map<UserProperty, String>) {
        if (!enabled) return

        parameters.forEach {
            savedPropertyList[it.key] = it.value
        }
    }

    public fun wasMetricTracked(event: EventName, parameters: Map<EventParameter, String> = emptyMap()): Boolean {
        if (!savedEventList.contains(event)) return false.also { println("Event $event not found in saved list") }
        // for eventName in saved list, get parameters and
        savedEventList[event]?.forEach { arg ->
            // check it's value to arguments parameters
            if (arg.value != parameters[arg.key]) return false.also { println("Parameter ${arg.key} with value ${arg.value} does not match expected value ${parameters[arg.key]} for event $event") }
        } ?: return false
        return true
    }

    override fun enable() {
        enabled = true
    }

    override fun disable() {
        enabled = false
    }
}

public class MockingLifecycleAwareProvider(
    name: Provider = Provider.testProvider,
    public var activityStarted: Boolean = false,
    public var activityStopped: Boolean = false,
): MockingMappedProvider(name), LifecycleAwareProvider {
    override fun onActivityStart(activityContext: Context) {
        activityStarted = true
    }

    override fun onActivityStop(activityContext: Context) {
        activityStopped = true
    }
}
