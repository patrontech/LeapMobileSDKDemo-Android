package com.greencopper.core.conditions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

public typealias  ConditionParameters = JsonElement

@Serializable
public data class ConditionSet(val predicate: String, val conditions: Map<String, ConditionInfo>)

