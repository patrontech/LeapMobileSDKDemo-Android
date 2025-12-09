package com.greencopper.core.remotestate

import java.util.concurrent.ConcurrentLinkedQueue
import com.greencopper.core.localstorage.LocalStorageProperty

internal class PersistedQueue(
    private val property: LocalStorageProperty<List<RemoteStateEntry>>
) : ConcurrentLinkedQueue<RemoteStateEntry>() {

    init {
        addAll(property.value)
    }

    fun save() {
        property.value = toList()
    }
}