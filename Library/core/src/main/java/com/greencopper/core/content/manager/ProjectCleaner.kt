package com.greencopper.core.content.manager

public interface ProjectCleaner {
    public suspend fun cleanProjectsData(projectsToKeep: Set<String>)
}
