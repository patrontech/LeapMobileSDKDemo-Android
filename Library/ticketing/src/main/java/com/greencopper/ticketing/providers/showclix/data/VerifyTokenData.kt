package com.greencopper.ticketing.providers.showclix.data

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class VerifyTokenData(val data: Data): KiboSerializable<VerifyTokenData> {

    override fun getSerializer(): KSerializer<VerifyTokenData> = serializer()

    @Serializable
    public data class Data(val id: String, val attributes: Attributes) {
        @Serializable
        public data class Attributes(
            val email: String,
            @SerialName("validation_token") val validationToken: String
        )
    }
}
