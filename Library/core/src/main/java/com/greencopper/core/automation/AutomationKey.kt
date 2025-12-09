package com.greencopper.core.automation

import com.greencopper.core.content.Key
import kotlinx.serialization.Serializable

@Serializable
public data class AutomationKey(
    override val name: String,
    override val version: Int,
) : Key()
