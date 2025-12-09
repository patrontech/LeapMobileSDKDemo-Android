package com.greencopper.interfacekit.appreview.commands

import androidx.fragment.app.DialogFragment
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.play.core.review.testing.FakeReviewManager
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.appreview.localstorage.appReviewRequests
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RequestAppReviewCommandTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val context = InstrumentationRegistry.getInstrumentation().context

    private val localStorage: LocalStorage = App.resolve()
    private val buildConfigProvider = MockBuildConfigProvider(
        mockVersionName = "123"
    )
    private val origin = mockk<DialogFragment>()

    private val command: RequestAppReviewCommand = RequestAppReviewCommand(
        localStorage,
        buildConfigProvider,
        FakeReviewManager(context),
        testScope
    )

    override fun afterEach() {}

    @Test
    fun executeCommand_withLayout_doesCommand() {
        every { origin.activity } returns mockk()

        runTest {
            command.execute(origin)
        }

        runBlocking {
            delay(500)
        }
        assertThat(localStorage.app.interfaceKit.appReviewRequests.requests.value).isNotEmpty
    }

    @Test
    fun executeCommand_withoutLayout_doesNothing() {
        every { origin.activity } returns mockk()

        runTest {
            command.execute(origin = null)
        }

        runBlocking {
            delay(500)
        }
        assertThat(localStorage.app.interfaceKit.appReviewRequests.requests.value).isEmpty()
    }

    @Test
    fun executeCommand_withMissingActivity_doesNothing() {
        runTest {
            command.execute(origin = null)
        }

        runBlocking {
            delay(500)
        }
        assertThat(localStorage.app.interfaceKit.appReviewRequests.requests.value).isEmpty()
    }
}
