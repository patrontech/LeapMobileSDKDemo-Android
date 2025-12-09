package com.greencopper.thuzi.account.registration.model

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class CompletionData(
   public val attendeeHasAuthenticated: Boolean
): KiboSerializable<CompletionData> {
    override fun getSerializer(): KSerializer<CompletionData> = serializer()
}
