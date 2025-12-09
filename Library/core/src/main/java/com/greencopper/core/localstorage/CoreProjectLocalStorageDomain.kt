package com.greencopper.core.localstorage

public class CoreProjectLocalStorageDomain(
    parent: ProjectLocalStorageDomain
): LocalStorageDomainBase("core", parent)

public val ProjectLocalStorageDomain.core: CoreProjectLocalStorageDomain
    get() = CoreProjectLocalStorageDomain(this)
