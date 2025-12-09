package com.greencopper.interfacekit.interests.viewmodel

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.testmocks.MockStore
import com.greencopper.testmocks.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

internal class InterestsViewModelTest {


    private val store = MockStore<InterestsState, InterestsAction>(InterestsState())

    private val viewModel = InterestsViewModel(
        viewBuilder = mockk(),
        store = store,
    )

    @Test
    fun setupView_sendsLoadInitialState() {
        val data = InterestsLayoutData("", "", ScreenNameAnalytics(""), null)
        viewModel.setupView(data)

        store.actionSent.size shouldBe  1
        store.actionSent.last() shouldBe InterestsAction.LoadInitialState(data)
    }

    @Test
    fun onInterestClicked_sendsInterestTapped() {
        val id = "testId"
        viewModel.onInterestClick(id, true)

        store.actionSent.size shouldBe 1
        store.actionSent.last() shouldBe InterestsAction.InterestTapped(id, true)
    }

    @Test
    fun onInterestsClosed_sendsInterestClosed() {
        viewModel.onInterestsClosed()

        store.actionSent.size shouldBe 1
        store.actionSent.last() shouldBe InterestsAction.InterestsClosed
    }
}
