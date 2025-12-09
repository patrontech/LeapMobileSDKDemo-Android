package com.greencopper.core.localstorage

public class ProjectLocalStorageDomain internal constructor(
    project: String,
    public override val localStorageContainer: LocalStorageContainer
): LocalStorageDomain {
    init {
        if (project == "@") throw IllegalArgumentException("@ is not a valid project name.")
    }

    public override val localStorageDomainName: LocalStorageName = LocalStorageName(project)
    public override val localStorageDomainParent: LocalStorageDomain? = null
}