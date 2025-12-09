package com.greencopper.testmocks.core

import com.greencopper.core.content.initialcontent.ContentInitializer
import com.greencopper.core.content.manager.Content
import com.greencopper.toolkit.testing.unimplemented

public class MockContentInitializer(
    public var initializeResult: () -> Content = { unimplemented() },
) : ContentInitializer {

    public var initializeCount: Int = 0

    override suspend fun initialize(): Content = initializeResult.invoke().also { initializeCount++ }
}
