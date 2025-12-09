package com.greencopper.core.services.manager

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.services.iplocation.IPLocationService
import com.greencopper.core.services.iplocation.RestrictedArea
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ConcreteServiceManager(
    switches: List<ServiceSwitch>,
    private val configurationHolder: ServicesConfigurationHolder,
    ipLocationService: IPLocationService,
    private val localStorage: LocalStorage,
    private val coroutineScope: CoroutineScope
): ServiceManager {
    private val lock = Mutex()

    /**
     * This is the list of all [ServiceSwitch] instances loaded from DI,
     * whether they are in `enabledAndroidServices` or not.
     */
    private val knownSwitches: Map<ServiceAspect, ServiceSwitch> = switches.associateBy { it.aspect }

    private var enabledSwitches: Map<ServiceAspect, ServiceSwitch> = emptyMap()

    init {
        coroutineScope.launch {
            lock.withLock {
                switches.forEach { it.enable(false) }
            }
            ipLocationService
                .completedFlow
                .filter { it }
                .flatMapLatest { configurationHolder.currentConfiguration }
                .filterNotNull()
                .collectLatest(::applyConfiguration)
        }
    }

    override fun enable(aspects: Set<ServiceAspect>, enabled: Boolean) {
        coroutineScope.launch {
            lock.withLock {
                aspects.forEach { enabledSwitches[it]?.enable(enabled) }
            }
        }
    }

    private fun applyConfiguration(config: ServicesConfiguration) {
        coroutineScope.launch {
            lock.withLock {
                val enabledSwitches = hashMapOf<ServiceAspect, ServiceSwitch>()
                val enabledServices = config.enabledAndroidServices.mapKeys { ServiceAspect(it.key) }
                for ((aspect, service) in knownSwitches) {
                    if (!enabledServices.containsKey(aspect)) {
                        /*
                         If a service isn't in the config, we disable it.
                         This is the only way to disable a service. `startAtLaunch`
                         has a different purpose.
                         */
                        service.enable(false)
                    } else {
                        enabledSwitches[aspect] = service
                        if (!this@ConcreteServiceManager.enabledSwitches.containsKey(aspect)) {
                            val start = enabledServices[aspect]!!.start
                            val location = localStorage.app.core.iplocation.value?.location ?: RestrictedArea.IN_RESTRICTED_AREA
                            val startAtLaunch = start.startAtLaunch(location)
                            /*
                             `startAtLaunch` is only enabled at launch. If the aspect is
                             already known, i.e., in `this.enabledSwitches`, we don't
                             change the user's preference.

                             Disabling a service is done by completely removing it from the
                             config, not by changing the value of `startAtLaunch`.
                             */
                            service.enable(startAtLaunch)
                        }
                    }
                }
                this@ConcreteServiceManager.enabledSwitches = enabledSwitches
            }
        }
    }
}
