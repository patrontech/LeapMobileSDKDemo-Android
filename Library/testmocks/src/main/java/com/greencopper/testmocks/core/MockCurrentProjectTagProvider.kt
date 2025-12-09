package com.greencopper.testmocks.core

import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.coroutines.flow.Flow

public class MockCurrentProjectTagProvider(
    public var currentProjectImpl: () -> String? = { unimplemented() },
    public var currentProjectFlowImpl: () -> Flow<String?> = { unimplemented() }
) : CurrentProjectTagProvider {
    override val currentProject: String? get()= currentProjectImpl()
    override val currentProjectFlow: Flow<String?> get()= currentProjectFlowImpl()
}
