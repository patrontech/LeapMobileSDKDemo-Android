package com.greencopper.testmocks.interfacekit

import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.projectswitcher.ProjectParams
import com.greencopper.core.content.projectswitcher.ProjectSwitcher

public class MockProjectSwitcher : ProjectSwitcher {

    public var lastSwitchProject: ProjectParams? = null
    public var mockContent: Content? = null
    public var mockSwitchProjectException: Throwable? = null

    override suspend fun switchProject(params: ProjectParams): Content? {
        lastSwitchProject = params
        mockSwitchProjectException?.let {
            throw it
        }
        return mockContent
    }

    public var currentProject: String = "currentProject"
    override fun isCurrentProject(project: String): Boolean {
        return currentProject == project
    }
}
