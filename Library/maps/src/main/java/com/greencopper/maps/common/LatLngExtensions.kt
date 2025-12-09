package com.greencopper.maps.common

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

internal fun List<LatLng>.toBounds(): LatLngBounds = LatLngBounds.builder().apply {
    forEach(::include)
}.build()
