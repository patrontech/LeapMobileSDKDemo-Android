package com.greencopper.interfacekit.search.logic

import com.greencopper.core.content.Key
import kotlinx.serialization.Serializable

@Serializable
public data class SearchProviderKey(override val name: String, override val version: Int) : Key()
