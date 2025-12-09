package com.greencopper.thuzi.logout

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockStore
import com.greencopper.testmocks.interfacekit.MockRootLayoutManager
import com.greencopper.testmocks.shouldBe
import com.greencopper.thuzi.mocks.MockThuziRegistrationManager
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class LogoutViewModelTest : CoroutineTest() {

    private val store = MockStore<LogoutState, LogoutAction>(LogoutState())
    private val registrationManager = MockThuziRegistrationManager()
    private val rootLayoutManager = MockRootLayoutManager()

    private val viewModel = LogoutViewModel(
        viewBuilder = mockk(),
        store = store,
        registrationManager = registrationManager,
        rootLayoutManager = rootLayoutManager,
    )

    override fun afterEach() {}

    @Test
    fun setupView() {
        val data = LogoutLayoutData(ScreenNameAnalytics(""))

        viewModel.setupView(data)

        store.actionSent.size shouldBe 1
        store.actionSent.last() shouldBe LogoutAction.LoadInitialState(data)
    }

    @Test
    fun logout() {
        runTest {
            viewModel.logout()

            store.actionSent.size shouldBe 1
            store.actionSent.last() shouldBe LogoutAction.LogoutTapped

            registrationManager.logoutCalled shouldBe true
            rootLayoutManager.updateRootLayoutCalled shouldBe true
        }
    }
}
