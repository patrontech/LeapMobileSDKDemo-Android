package com.greencopper.core.content.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

public interface ContentHistory {
    public val currentProject: String?
    public val currentProjectFlow: SharedFlow<String?>
    public var currentContent: Content?
    public val currentContentFlow: Flow<Content?>
    public var forcedContent: Content?
    public val contents: Set<Content>

    public suspend fun saveContent(content: Content)
    public suspend fun saveHistory()
}
