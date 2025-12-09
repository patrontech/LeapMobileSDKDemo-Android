package com.greencopper.thuzi.account.deletion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class AccountDeletionViewModel(
    private val deleteAccountService: DeleteAccountService,
    // Here for tests, should be CONFIRM by default
    private val _state: MutableStateFlow<State> = MutableStateFlow(State.CONFIRM),
    private val aggregateMetricsService: AggregateMetricsService,
    private val coroutineDispatcher: CoroutineDispatcher,
) : ViewModel() {
    internal enum class State {
        CONFIRM,
        LOADING,
        SUCCESS,
        FAIL,
        RESTART_APP,
    }

    val state: Flow<State> = _state

    fun handlePrimaryButtonClick(apiUrl: String) {
        when (_state.value) {
            State.LOADING, State.RESTART_APP -> return
            State.SUCCESS -> _state.value = State.RESTART_APP
            else -> {
                // if state is CONFIRM or FAIL should try to delete account
                if (_state.value == State.CONFIRM) {
                    aggregateMetricsService
                        .track(AccountDeletionConfirmedEvent())
                } else {
                    aggregateMetricsService
                        .track(AccountDeletionRetryEvent())
                }

                _state.value = State.LOADING
                viewModelScope.launch(coroutineDispatcher) {
                    when (deleteAccountService.deleteAccount(apiUrl)) {
                        DeleteAccountResult.SUCCESS -> {
                            aggregateMetricsService
                                .track(AccountDeletionSuccessEvent())
                            _state.value = State.SUCCESS
                        }
                        DeleteAccountResult.FAIL -> {
                            aggregateMetricsService
                                .track(AccountDeletionFailEvent())
                            _state.value = State.FAIL
                        }
                    }
                }
            }
        }
    }

    internal class AccountDeletionConfirmedEvent : AccountDeletionEvent("account_deletion/confirmed")
    internal class AccountDeletionRetryEvent : AccountDeletionEvent("account_deletion/retry")
    internal class AccountDeletionSuccessEvent : AccountDeletionEvent("account_deletion/success")
    internal class AccountDeletionFailEvent : AccountDeletionEvent("account_deletion/fail")

    internal open class AccountDeletionEvent(private val eventName: String) : MappedMetrics {
        override fun track(provider: MappedProvider) {
            val eventName = EventName(eventName)
            provider.track(eventName, emptyMap())
        }
    }
}
