package com.greencopper.thuzi.account.deletion

import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.thuzi.account.deletion.AccountDeletionViewModel.*
import com.greencopper.thuzi.mocks.MockDeleteAccountService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AccountDeletionViewModelTest : CoroutineTest(UnconfinedTestDispatcher()) {
    private val deleteAccountService = MockDeleteAccountService()
    private val state: MutableStateFlow<State> = MutableStateFlow(State.CONFIRM)
    private val aggregateMetricsService = MockAggregateMetricsService()
    private val viewModel = AccountDeletionViewModel(
        deleteAccountService,
        state,
        aggregateMetricsService,
        Dispatchers.Unconfined
    )

    override fun afterEach() = Unit

    @Test
    fun whenHandlePrimaryButtonClick_inConfirmState_shouldLaunchAccountDeletion_whenDeletionSuccess() {
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionConfirmedEvent::class)).isTrue
        assertThat(deleteAccountService.lastDeleteUrl).isEqualTo("apiUrl")
        assertThat(state.value).isEqualTo(State.SUCCESS)
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionSuccessEvent::class)).isTrue
    }

    @Test
    fun whenHandlePrimaryButtonClick_inConfirmState_shouldLaunchAccountDeletion_whenDeletionFail() {
        deleteAccountService.mockResult = DeleteAccountResult.FAIL
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionConfirmedEvent::class)).isTrue
        assertThat(state.value).isEqualTo(State.FAIL)
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionFailEvent::class)).isTrue
    }

    @Test
    fun whenHandlePrimaryButtonClick_inLoadingState_shouldBeSkipped() {
        state.value = State.LOADING
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(state.value).isEqualTo(State.LOADING)
    }

    @Test
    fun whenHandlePrimaryButtonClick_inSuccessState_shouldRestartApp() {
        state.value = State.SUCCESS
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(state.value).isEqualTo(State.RESTART_APP)
    }

    @Test
    fun whenHandlePrimaryButtonClick_inFailState_shouldLaunchAccountDeletion_whenDeletionSuccess() {
        state.value = State.FAIL
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionRetryEvent::class)).isTrue
        assertThat(deleteAccountService.lastDeleteUrl).isEqualTo("apiUrl")
        assertThat(state.value).isEqualTo(State.SUCCESS)
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionSuccessEvent::class)).isTrue
    }

    @Test
    fun whenHandlePrimaryButtonClick_inFailState_shouldLaunchAccountDeletion_whenDeletionFail() {
        state.value = State.FAIL
        deleteAccountService.mockResult = DeleteAccountResult.FAIL
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionRetryEvent::class)).isTrue
        assertThat(state.value).isEqualTo(State.FAIL)
        assertThat(aggregateMetricsService.wasMetricTracked(AccountDeletionFailEvent::class)).isTrue
    }

    @Test
    fun whenHandlePrimaryButtonClick_inRestartAppState_shouldBeSkipped() {
        state.value = State.RESTART_APP
        viewModel.handlePrimaryButtonClick("apiUrl")
        assertThat(state.value).isEqualTo(State.RESTART_APP)
    }
}
