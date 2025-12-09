package com.greencopper.maps.metrics

import com.greencopper.core.metrics.Screen

internal fun Screen.Companion.geoMap(name: String): Screen = Screen(name = name, klass = "geo_map")
internal fun Screen.Companion.locationDetail(name: String): Screen = Screen(name = name, klass = "location_detail")
internal fun Screen.Companion.locationList(name: String): Screen = Screen(name = name, klass = locationsList_class)
internal const val locationsList_class = "locations_list"
