package com.greencopper.toolkit.logging

import android.util.Log
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.BuildConfig
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.logging.multilogging.MultiLoggingConfigurationsImpl
import com.greencopper.toolkit.logging.multilogging.configurations.ConsoleLoggingConfiguration
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ConsoleLoggingConfigurationTest {
    private val consoleLog: StringBuilder = StringBuilder("")

    private val buildConfigProvider = MockBuildConfigProvider()

    private val loggingImpl: Logging =
        MultiLoggingConfigurationsImpl().apply { addConfiguration(ConsoleLoggingConfiguration()) }

    private val tagSlot = slot<String>()
    private val messageSlot = slot<String>()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        mockkStatic(Log::class)
        every {
            Log.wtf(capture(tagSlot), capture(messageSlot))
        } answers {
            consoleLog.append("${tagSlot.captured} ${messageSlot.captured}\n")
            0
        }

        every {
            Log.println(any(), capture(tagSlot), capture(messageSlot))
        } answers {
            consoleLog.append("${tagSlot.captured} ${messageSlot.captured}\n")
            0
        }

        bindSingleton<BuildConfigProvider>(buildConfigProvider)

        buildConfigProvider.mockIsDebug = true
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic(Log::class)
        buildConfigProvider.mockIsDebug = BuildConfig.DEBUG
    }

    @Test
    fun consoleLogging() {
        val debugMessage = "DebugMessage"
        loggingImpl.d(debugMessage)
        assertThat(consoleLog).contains(debugMessage)

        val assertMessage = "AssertMessage"
        loggingImpl.log(LogLevel.ASSERT, assertMessage)
        assertThat(consoleLog).contains(assertMessage)
    }

    @Test
    fun consoleLogging_WhenRelease() {
        buildConfigProvider.mockIsDebug = false
        val debugMessage = "DebugMessage"
        loggingImpl.d(debugMessage)
        assertThat(consoleLog).doesNotContain(debugMessage)

        val assertMessage = "AssertMessage"
        loggingImpl.log(LogLevel.ASSERT, assertMessage)
        assertThat(consoleLog).doesNotContain(assertMessage)
    }
}
