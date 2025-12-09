package com.greencopper.interfacekit.list.provider

import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.interfacekit.list.initializer.ListMode

public data class EventFavoritesListAnalytics(
    val eventName: EventName,
    val screenName: String,
    val itemId: String,
    val itemName: String,
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val parameters = mapOf(
            EventParameter.screenName to screenName,
            EventParameter.itemId to itemId,
            EventParameter.itemName to itemName
        )
        provider.track(
            eventName,
            parameters
        )
    }
}

public data class ScreenListEvent(
    val name: String,
    val klass: String,
    val listMode: String,
    val favoritesOnly: Boolean,
    val filteringPredicate: String?,
) : ScreenViewEvent(
    Screen(name, klass),
    mapOf(
        EventParameter("displayMode") to listMode,
        EventParameter("favoritesOnly") to favoritesOnly.toString(),
        EventParameter("filteringPredicate") to filteringPredicate.toString(),
    )
) {
    public constructor(
        name: String,
        klass: String,
        listMode: ListMode,
        favoritesOnly: Boolean,
        filteringPredicate: String?,
    ) : this(
        name,
        klass,
        listMode.type,
        favoritesOnly,
        filteringPredicate
    )
}
