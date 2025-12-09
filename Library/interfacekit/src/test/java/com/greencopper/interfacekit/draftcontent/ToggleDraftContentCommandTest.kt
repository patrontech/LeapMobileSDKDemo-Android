package com.greencopper.interfacekit.draftcontent

import com.greencopper.testmocks.core.MockDraftContentManager
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ToggleDraftContentCommandTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val draftContentManager = MockDraftContentManager()
    private val routeController = MockRouteController()

    private val command = ToggleDraftContentCommand(
        draftContentManager = draftContentManager,
        routeController = routeController,
        localizationService = MockLocalizationService(),
        logging = App.resolve(),
        backgroundScope = testScope,
    )

    override fun afterEach() {}

    @Test
    fun noPasscodeStored_toggle_asksForInput() {
        draftContentManager.passcodeReturnValue = { null }

        runBlocking {
            command.execute()
            delay(500)

            assertThat(routeController.showInputAlertCalled).isTrue
        }
    }

    @Test
    fun passcodeStored_toggle_asksForConfirmation() {
        draftContentManager.passcodeReturnValue = { "passcode" }

        runBlocking {
            command.execute()
            delay(500)

            assertThat(routeController.showAlertCalled).isTrue
        }
    }
}
