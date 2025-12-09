package com.greencopper.core.automation

import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e

public abstract class AutomationRunnerException(cause: Throwable? = null) : Throwable(cause) {
    public class AutomationNotRegistered(key: AutomationKey, cause: Throwable? = null) : AutomationRunnerException(cause) {
        override val message: String = "Automation not registered ($key) : \n $cause"
    }

    public class AutomationSetupFailed(key: AutomationKey, cause: Throwable? = null) : AutomationRunnerException(cause) {
        override val message: String = "Automation setup failed ($key) : \n $cause"
    }
}

public interface AutomationRunner {
    public fun setup(automations: List<AutomationInfo>)
}

internal class ConcreteAutomationRunner(
    private val resolver: Resolver,
    private val logger: Logging,
) : AutomationRunner {

    var runningAutomations: List<Automation> = emptyList()

    override fun setup(automations: List<AutomationInfo>) {
        runningAutomations = automations
            .mapNotNull {
                try {
                    setup(it)
                } catch (e: AutomationRunnerException) {
                    logger.e("Automation setup failed", throwable = e)
                    null
                }
            }
    }

    private fun setup(automationInfo: AutomationInfo): Automation {
        val automation = try {
            resolver.resolve<Automation>(tag = automationInfo.key)
        } catch (t: Throwable) {
            throw AutomationRunnerException.AutomationNotRegistered(automationInfo.key, t)
        }

        return try {
            automation.setup(automationInfo.params)
            automation
        } catch (t: Throwable) {
            throw AutomationRunnerException.AutomationSetupFailed(automationInfo.key, t)
        }
    }
}
