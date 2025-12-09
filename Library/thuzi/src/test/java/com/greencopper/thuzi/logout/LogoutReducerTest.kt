package com.greencopper.thuzi.logout

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.shouldBe
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import com.toggl.komposable.test.testReduceNoOp
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class LogoutReducerTest : CoroutineTest() {

    private val logoutReducer = LogoutReducer(
        localizationService = MockLocalizationService(
            getStringFromRepository = { _ -> "mockResult" }
        ),
    )

    override fun afterEach() { }

    @Test
    fun givenEmptyState_actionLoadInitialState_returnsLayoutState() = runTest {
        val params = LogoutLayoutData(ScreenNameAnalytics("logoutScreen"))
        logoutReducer.testReduce(LogoutState(), LogoutAction.LoadInitialState(params)) { state, effect ->
            state.title shouldBe "mockResult"
            state.subtitle shouldBe "mockResult"
            state.buttonText shouldBe "mockResult"
            effect shouldBe NoEffect
        }
    }

    @Test
    fun actionLogoutTapped_noEffect() = runTest {
        logoutReducer.testReduceNoOp(LogoutState(), LogoutAction.LogoutTapped)
    }
}
