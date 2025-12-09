package com.greencopper.core.metrics.events

import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider

public val EventName.Companion.screenView: EventName by lazy { EventName("screen_view") }
public val EventParameter.Companion.screenName: EventParameter by lazy { EventParameter("screen_name") }
public val EventParameter.Companion.screenClass: EventParameter by lazy { EventParameter("screen_class") }

public open class ScreenViewEvent(
    private val screen: Screen,
    parameters: Map<EventParameter, String> = emptyMap(),
) : MappedMetrics {

    private val internalParameters = parameters.toMutableMap()

    public override fun track(provider: MappedProvider) {
        internalParameters[EventParameter.screenName] = screen.name
        internalParameters[EventParameter.screenClass] = screen.klass
        provider.track(EventName.screenView, internalParameters)
    }
}
