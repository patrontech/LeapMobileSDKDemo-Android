package com.greencopper.core.content.localstorage

import com.greencopper.core.content.manager.Content
import com.greencopper.core.localstorage.*

internal class ContentProjectLocalStorageDomain(
    parent: CoreProjectLocalStorageDomain
): LocalStorageDomainBase("content", parent) {
    internal val forcedContent: LocalStorageProperty<Content?>
        by localStorageProperty(null)
    internal val contents: LocalStorageProperty<Set<Content>>
        by localStorageProperty(emptySet())
}

internal val CoreProjectLocalStorageDomain.content: ContentProjectLocalStorageDomain
    get() = ContentProjectLocalStorageDomain(this)