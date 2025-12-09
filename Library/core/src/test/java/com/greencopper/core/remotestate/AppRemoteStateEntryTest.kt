package com.greencopper.core.remotestate

import com.greencopper.core.remotestate.models.AppRemoteStateEntry
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AppRemoteStateEntryTest {
    private val remoteStateDispatcher: RemoteStateDispatcher = mockk()
    private val buildConfigProvider = MockBuildConfigProvider()

    @BeforeEach
    fun setupEach() {
        every { remoteStateDispatcher.dispatch(any()) } returns Unit
    }

    @Test
    fun dispatchAppRemoteState_success() {
        val entry = AppRemoteStateEntry(
            appVersion = "1.0.0",
            buildConfig = buildConfigProvider
        )
        entry.dispatch(remoteStateDispatcher)
        verify(exactly = 4) { remoteStateDispatcher.dispatch(any()) }
    }
}
