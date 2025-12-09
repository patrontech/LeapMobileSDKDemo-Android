package com.greencopper.core.conditions

import kotlinx.serialization.Serializable
import com.greencopper.core.content.Key as CoreContentKey

@Serializable
public data class ConditionInfo(
    val key: Key,
    val params: ConditionParameters? = null,
    val fallback: Boolean
) {
    @Serializable
    public data class Key(override val name: String, override val version: Int) : CoreContentKey()
}
