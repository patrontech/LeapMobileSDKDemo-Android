package com.greencopper.core.content.projectswitcher

import com.greencopper.core.content.manager.Content
import kotlinx.coroutines.flow.Flow

public interface ProjectSwitcher {
    public suspend fun switchProject(params: ProjectParams): Content?
    public fun isCurrentProject(project: String): Boolean
}
