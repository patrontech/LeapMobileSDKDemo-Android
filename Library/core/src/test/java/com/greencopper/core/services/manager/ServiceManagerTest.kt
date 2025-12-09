package com.greencopper.core.services.manager

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.services.iplocation.IPLocation
import com.greencopper.core.services.iplocation.RestrictedArea
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockIPLocationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private typealias Start = ServicesConfiguration.Start

internal class ServiceManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {
    class UselessServiceSwitch: ServiceSwitch {
        var enabled: Boolean = false

        override val aspect: ServiceAspect = ServiceAspect.useless

        override fun enable(enabled: Boolean) {
            this.enabled = enabled
        }
    }

    class WorthlessServiceSwitch: ServiceSwitch {
        var enabled: Boolean = false

        override val aspect: ServiceAspect = ServiceAspect.worthless

        override fun enable(enabled: Boolean) {
            this.enabled = enabled
        }
    }

    private val servicesConfigurationHolder = ServicesConfigurationHolder()
    private val ipLocationService = MockIPLocationService()

    private val localStorage: LocalStorage

    init {
        Toolkit.setupTest()
        servicesConfigurationHolder.currentConfiguration.value = null
        localStorage = LocalStorage("project")
    }

    override fun afterEach() {}

    @Test
    fun `ServiceManager disables known services at launch`() {
        val switch = UselessServiceSwitch()
        switch.enable(true)

        ConcreteServiceManager(
            listOf(switch),
            servicesConfigurationHolder,
            ipLocationService,
            localStorage,
            testScope
        )

        assertThat(switch.enabled).isFalse
    }

    @Test
    fun `ServiceManager disables unknown service after config change`() {
        val switch = UselessServiceSwitch()

        val manager = ConcreteServiceManager(
            listOf(switch),
            servicesConfigurationHolder,
            ipLocationService,
            localStorage,
            testScope
        )

        var config = ServicesConfiguration(mapOf("useless" to ServicesConfiguration.Settings(Start.AT_LAUNCH)))
        servicesConfigurationHolder.currentConfiguration.value = config
        assertThat(switch.enabled).isTrue

        config = ServicesConfiguration(emptyMap())
        servicesConfigurationHolder.currentConfiguration.value = config
        assertThat(switch.enabled).isFalse

        // If a switch has been removed from the config, it cannot be enabled.
        manager.enable(setOf(ServiceAspect.useless), true)
        assertThat(switch.enabled).isFalse
    }

    @Test
    fun `ServiceManager launches a new service after a config change`() {
        val useless = UselessServiceSwitch()
        val worthless = WorthlessServiceSwitch()

        ConcreteServiceManager(
            listOf(useless, worthless),
            servicesConfigurationHolder,
            ipLocationService,
            localStorage,
            testScope
        )
        assertThat(useless.enabled).isFalse
        assertThat(worthless.enabled).isFalse

        var config = ServicesConfiguration(mapOf("useless" to ServicesConfiguration.Settings(Start.AT_LAUNCH)))
        servicesConfigurationHolder.currentConfiguration.value = config
        assertThat(useless.enabled).isTrue
        assertThat(worthless.enabled).isFalse

        config = ServicesConfiguration(mapOf(
            "useless" to ServicesConfiguration.Settings(Start.AT_LAUNCH),
            "worthless" to ServicesConfiguration.Settings(Start.AT_LAUNCH),
        ))
        servicesConfigurationHolder.currentConfiguration.value = config
        assertThat(useless.enabled).isTrue
        assertThat(worthless.enabled).isTrue
    }

    @Test
    fun `ServiceManager enables service in restricted area if in restricted area`() {
        val useless = UselessServiceSwitch()
        val worthless = WorthlessServiceSwitch()

        localStorage.app.core.iplocation.value = IPLocation(
            "NA",
            "CA",
            RestrictedArea.IN_RESTRICTED_AREA
        )
        ConcreteServiceManager(
            listOf(useless, worthless),
            servicesConfigurationHolder,
            ipLocationService,
            localStorage,
            testScope
        )
        assertThat(useless.enabled).isFalse
        assertThat(worthless.enabled).isFalse

        val config = ServicesConfiguration(mapOf(
            "useless" to ServicesConfiguration.Settings(Start.IN_RESTRICTED_AREA),
            "worthless" to ServicesConfiguration.Settings(Start.OUTSIDE_RESTRICTED_AREA),
        ))
        servicesConfigurationHolder.currentConfiguration.value = config
        assertThat(useless.enabled).isTrue
        assertThat(worthless.enabled).isFalse
    }

    @Test
    fun `ServiceManager enables service outside restricted area if outside restricted area`() {
        val useless = UselessServiceSwitch()
        val worthless = WorthlessServiceSwitch()
        val configHolder = ServicesConfigurationHolder()

        localStorage.app.core.iplocation.value = IPLocation(
            "NA",
            "CA",
            RestrictedArea.OUTSIDE_RESTRICTED_AREA
        )
        ConcreteServiceManager(
            listOf(useless, worthless),
            configHolder,
            ipLocationService,
            localStorage,
            testScope
        )
        assertThat(useless.enabled).isFalse
        assertThat(worthless.enabled).isFalse

        val config = ServicesConfiguration(mapOf(
            "useless" to ServicesConfiguration.Settings(Start.IN_RESTRICTED_AREA),
            "worthless" to ServicesConfiguration.Settings(Start.OUTSIDE_RESTRICTED_AREA),
        ))
        configHolder.currentConfiguration.value = config
        assertThat(useless.enabled).isFalse
        assertThat(worthless.enabled).isTrue
    }
}

internal val ServiceAspect.Companion.useless: ServiceAspect
    by lazy { ServiceAspect("useless") }

internal val ServiceAspect.Companion.worthless: ServiceAspect
    by lazy { ServiceAspect("worthless") }
