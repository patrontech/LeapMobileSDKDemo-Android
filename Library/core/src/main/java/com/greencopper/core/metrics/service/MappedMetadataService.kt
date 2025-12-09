package com.greencopper.core.metrics.service

import com.greencopper.core.metrics.labels.EventParameter

public class MappedMetadataService {
    internal val metadatas = mutableMapOf<EventParameter, String>()

    public operator fun get(eventParameter: EventParameter): String? = metadatas[eventParameter]
    public operator fun set(eventParameter: EventParameter, value: String) {
        metadatas[eventParameter] = value
    }
}
