package com.greencopper.event.common

import com.greencopper.core.localstorage.LocalStorageDomainBase
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.localstorage.ProjectLocalStorageDomain
import com.greencopper.core.localstorage.localStorageProperty

internal class EventProjectLocalStorageDomain(parent: ProjectLocalStorageDomain) :
    LocalStorageDomainBase("event", parent) {

    val myScheduleItemIds: LocalStorageProperty<Set<Long>> by localStorageProperty(emptySet())
    val firstEventAdded: LocalStorageProperty<Boolean> by localStorageProperty(false)
    val reminderIntervalMins: LocalStorageProperty<Int?> by localStorageProperty(null)

    val myActivities: LocalStorageProperty<Set<Long>> by localStorageProperty(emptySet())
    val myPerformers: LocalStorageProperty<Set<String>> by localStorageProperty(emptySet())
}

internal val ProjectLocalStorageDomain.event: EventProjectLocalStorageDomain
    get() = EventProjectLocalStorageDomain(this)
