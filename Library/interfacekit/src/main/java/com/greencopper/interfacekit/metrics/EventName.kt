package com.greencopper.interfacekit.metrics

import com.greencopper.core.metrics.labels.EventName

internal fun EventName.Companion.widgetCollectionWidgetTap(): EventName = EventName("widget_collection/widget_tap")
internal fun EventName.Companion.widgetCollectionLinkTap(): EventName = EventName("widget_collection/link_tap")
internal fun EventName.Companion.adTap(): EventName = EventName("ad_tap")
internal fun EventName.Companion.adImpression(): EventName = EventName("ad_impression")
internal fun EventName.Companion.filterOptionTap(): EventName = EventName("filter_select")
internal fun EventName.Companion.locationPermission(): EventName = EventName("permission/location")
internal fun EventName.Companion.notificationPermission(): EventName = EventName("permission/notifications")
internal fun EventName.Companion.adOnboardingTap(): EventName = EventName("onboarding/ad/ad_tap")
internal fun EventName.Companion.adOnboardingClose(): EventName = EventName("onboarding/ad/close_tap")
