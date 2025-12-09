package com.greencopper.interfacekit.commands

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PresentAppStoreCommandTest {
    private val context: Context = mockk(relaxed = true)
    private val origin = mockk<Layout>()
    private val logger = MockLogging()
    private val localizationService = MockLocalizationService()
    private val presentAppStoreCommand = PresentAppStoreCommand(localizationService, logger)
    private val intentStarted = slot<Intent>()

    init {
        Toolkit.setupTest()
        every { origin.context } returns context
        every { context.startActivity(capture(intentStarted)) } returns Unit
    }

    @Test
    fun executeCommand_withOrigin_shouldStartActivity() = runTest {
        val params = PresentAppStoreCommand.PresentAppStoreParams("com.my.package")

        val flowResult = presentAppStoreCommand.executeWith(params, origin)

        val intent = intentStarted.captured
        intent.dataString shouldBe "market://details?id=com.my.package"
        intent.`package` shouldBe "com.android.vending"

        val value = withTimeoutOrNull(500) {
            flowResult.first()
        }

        value shouldBe null
    }

    @Test
    fun executeCommand_withoutOrigin_shouldReturn() = runTest {
        val params = PresentAppStoreCommand.PresentAppStoreParams("com.my.package")

        val flowResult = presentAppStoreCommand.executeWith(params, null)

        val value = withTimeoutOrNull(500) {
            flowResult.first()
        }

        value shouldBe null
    }

    @Test
    fun executeCommand_withStartActivityFailing_shouldReturn() = runTest {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every {
            Toast.makeText(any(), any<CharSequence>(), any())
        } returns toast

        every { context.startActivity(capture(intentStarted)) } throws RuntimeException("fail")
        val params = PresentAppStoreCommand.PresentAppStoreParams("com.my.package")

        val flowResult = presentAppStoreCommand.executeWith(params, origin)

        val value = withTimeoutOrNull(500) {
            flowResult.first()
        }

        value shouldBe null
        assertThat(localizationService.requestedKeys).contains("force_update.error.android")
    }
}
