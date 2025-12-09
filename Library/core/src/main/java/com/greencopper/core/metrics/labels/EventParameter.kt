package com.greencopper.core.metrics.labels

public data class EventParameter(private val name: String) : MappedName(name) {
    public companion object
}

public val EventParameter.Companion.itemId: EventParameter by lazy { EventParameter("item_id") }
public val EventParameter.Companion.itemName: EventParameter by lazy { EventParameter("item_name") }
public val EventParameter.Companion.itemCategory: EventParameter by lazy { EventParameter("item_category") }
public val EventParameter.Companion.uri: EventParameter by lazy { EventParameter("uri") }