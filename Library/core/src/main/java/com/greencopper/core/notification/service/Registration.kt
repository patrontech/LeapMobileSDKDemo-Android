package com.greencopper.core.notification.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Registration(
    @SerialName("registration_token")
    val registrationToken: String,
    val platform: String,
    val locale: String
)