package com.greencopper.testmocks.core

import com.greencopper.core.content.manager.ProjectCleaner

public class MockProjectCleaner(private val cleanProjectDataAnswer: (Set<String>) -> Unit = {}) :
    ProjectCleaner {
    public var cleanProjectDataCount: Int = 0

    override suspend fun cleanProjectsData(projectsToKeep: Set<String>): Unit =
        cleanProjectDataAnswer(projectsToKeep).also { cleanProjectDataCount++ }
}
