package com.greencopper.core.automation

import com.greencopper.toolkit.di.binding.Creator
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.container.Key

public fun Registrar.bindAutomation(
    key: AutomationKey,
    automation: Creator<Automation>
): Key = bindProvider(tag = key, automation)
