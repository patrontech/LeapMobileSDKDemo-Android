package com.greencopper.toolkit.logging

import com.greencopper.toolkit.logging.multilogging.MultiLoggingConfigurationsImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

internal class MultiLoggingConfigurationTest {

    private val loggingImpl: Logging = MultiLoggingConfigurationsImpl()
    private val mockLoggingConfiguration = MockLoggingConfiguration()

    @BeforeEach
    fun setupEach() {
        loggingImpl.removeAllConfigurations()
        loggingImpl.addConfiguration(mockLoggingConfiguration)
        cleanLog()
    }

    @Test
    fun removeConfiguration() {
        loggingImpl.removeConfiguration(mockLoggingConfiguration)
        assertThat(loggingImpl.configurations()).isEmpty()
        assertThatThrownBy {
            loggingImpl.removeConfiguration(
                mockLoggingConfiguration
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun whenLogging_atAssertLevel_shouldContainLog() {
        val logMessage = "Logging message"
        val logPriority = LogLevel.ASSERT
        loggingImpl.log(logPriority, logMessage)
        assertContains("Priority: ASSERT")
        assertContains("Message: $logMessage")
        assertContains(" Tag: MultiLoggingConfigurationTest:whenLogging_atAssertLevel_shouldContainLog")
        assertNotContains("Throwable:")
    }

    @Test
    fun whenLogging_atVerboseLevel_shouldContainLog() {
        val message = "defaultConstructorMessageVerbose"
        loggingImpl.v(message)
        assertContains("Priority: ${LogLevel.VERBOSE.name}")
        assertContains(" - Message: $message")
    }

    @Test
    fun whenLogging_atInfoLevel_shouldContainLog() {
        val message = "defaultConstructorMessageInfo"
        loggingImpl.i(message)
        assertContains("Priority: ${LogLevel.INFO.name}")
        assertContains(" - Message: $message")
    }

    @Test
    fun whenLogging_atDebugLevel_shouldContainLog() {
        val message = "defaultConstructorMessageDebug"
        loggingImpl.d(message)
        assertContains("Priority: ${LogLevel.DEBUG.name}")
        assertContains(" - Message: $message")
    }

    @Test
    fun whenLogging_atWarningLevel_shouldContainLog() {
        val message = "defaultConstructorMessageWarning"
        loggingImpl.w(message)
        assertContains("Priority: ${LogLevel.WARN.name}")
        assertContains(" - Message: $message")
    }

    @Test
    fun whenLogging_atErrorLevel_shouldContainLog() {
        val message = "defaultConstructorMessageError"
        loggingImpl.e(message)
        assertContains("Priority: ${LogLevel.ERROR.name}")
        assertContains(" - Message: $message")
    }

    @Test
    fun whenLogging_withStringFormat_shouldFormattedString() {
        loggingImpl.d(
            "My message is %s since %d years",
            null, null, "this great message", 100
        )
        assertContains("My message is this great message since 100 years")
    }

    @Test
    fun whenLogging_withException_shouldContainException() {
        val exception = Exception()
        loggingImpl.e("Message", throwable = exception)
        assertContains("Message")
        assertContains("$exception")
    }

    @Test
    fun whenLogging_withoutEverything_shouldSkip() {
        loggingImpl.d("")
        assertThat(mockLoggingConfiguration.executed).isFalse
    }

    @Test
    fun whenLogging_withoutException_shouldOnlyLogMessage() {
        loggingImpl.e("Message")
        assertContains("Message")
    }

    @Test
    fun whenLogging_withoutExceptionAndEmptyText_shouldLogStackTrace() {
        val exception = Exception()
        loggingImpl.e("", throwable = exception)
        assertContains(getStackTraceString(exception))
    }

    private fun cleanLog() {
        mockLoggingConfiguration.logContent = ""
    }

    private fun assertContains(text: String) {
        val content = mockLoggingConfiguration.logContent
        assertThat(content).contains(text)
    }

    private fun assertNotContains(text: String) {
        val content = mockLoggingConfiguration.logContent
        assertThat(content).doesNotContain(text)
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        throwable.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}