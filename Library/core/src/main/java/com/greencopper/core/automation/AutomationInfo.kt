package com.greencopper.core.automation

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class AutomationInfo(
    val key: AutomationKey,
    val params: AutomationParams? = null
): KiboSerializable<AutomationInfo> {

    override fun getSerializer(): KSerializer<AutomationInfo> = serializer()
}
