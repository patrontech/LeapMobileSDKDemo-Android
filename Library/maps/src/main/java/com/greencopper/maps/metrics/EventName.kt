package com.greencopper.maps.metrics

import com.greencopper.core.metrics.labels.EventName

internal fun EventName.Companion.geoMapPinClick() = EventName("geo_map/point_tap")
internal fun EventName.Companion.addToMyLocations(): EventName = EventName(addToMyLocations_name)
internal const val addToMyLocations_name = "my_locations/add"

internal fun EventName.Companion.removeFromMyLocations(): EventName = EventName(removeFromMyLocations_name)
internal const val removeFromMyLocations_name = "my_locations/remove"

