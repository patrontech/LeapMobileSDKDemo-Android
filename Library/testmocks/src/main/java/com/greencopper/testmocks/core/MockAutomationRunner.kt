package com.greencopper.testmocks.core

import com.greencopper.core.automation.AutomationInfo
import com.greencopper.core.automation.AutomationRunner
import com.greencopper.toolkit.testing.unimplemented

public class MockAutomationRunner(
    private var _setup: (List<AutomationInfo>) -> Unit = { unimplemented() },
) : AutomationRunner {
    override fun setup(automations: List<AutomationInfo>): Unit = _setup(automations)
}
