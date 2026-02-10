package com.example.kibasdkpoc.analytics

import com.greencopper.leapmobilesdk.core.metrics.labels.EventName
import com.greencopper.leapmobilesdk.core.metrics.labels.EventParameter
import com.greencopper.leapmobilesdk.core.metrics.labels.UserProperty
import com.greencopper.leapmobilesdk.core.metrics.provider.MappedProvider
import com.greencopper.leapmobilesdk.core.metrics.provider.Provider
import com.greencopper.leapmobilesdk.core.metrics.provider.default

public class MyAnalyticsProvider : MappedProvider {
    override val name: Provider = Provider("my_provider")

    override fun enable() {
        // Enable provider
    }

    override fun disable() {
        // Disable provider
    }

    override fun track(event: EventName, parameters: Map<EventParameter, String>) {
        val eventName = event[Provider.default] ?: "unknown_event"
        val paramsMap = parameters.mapKeys { it.key[Provider.default] ?: it.key.toString() }
            .mapValues { it.value }

        // Example with Firebase Analytics:
        // FirebaseAnalytics.getInstance(context).logEvent(eventName, Bundle().apply {
        //     paramsMap.forEach { (key, value) -> putString(key, value) }
        // })

        println("Tracking event: $eventName with params: $paramsMap")
    }

    override fun track(parameters: Map<UserProperty, String>) {
        parameters.forEach { (property, value) ->
            val propertyName = property[Provider.default] ?: property.toString()
            // FirebaseAnalytics.getInstance(context).setUserProperty(propertyName, value)
        }
    }

}