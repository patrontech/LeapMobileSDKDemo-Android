package com.greencopper.core.services.iplocation

import kotlinx.serialization.Serializable

@Serializable
public data class IPLocation(
    val country: String,
    val continent: String,
    val location: RestrictedArea,
)
