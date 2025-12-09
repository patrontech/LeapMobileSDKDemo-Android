package com.greencopper.core.content.initialcontent

import com.greencopper.core.content.manager.Content

public interface ContentInitializer {

    /** Initialize content across the app */
    public suspend fun initialize(): Content
}
