package com.greencopper.ticketing.ticketsscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.Provider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class TicketsScanViewModel(
    private val ticketingProvider: Provider,
    private val backgroundCoroutineContext: CoroutineContext,
) : ViewModel() {
    private val _ticketUiState = MutableSharedFlow<TicketsUiState>(replay = 1).apply {
        tryEmit(TicketsUiState.Success(null))
    }
    val ticketUiState: SharedFlow<TicketsUiState> = _ticketUiState

    fun getCacheTickets(): List<Ticket> = ticketingProvider.cachedTickets

    fun fetchTickets() = viewModelScope.launch(backgroundCoroutineContext) {
        try {
            _ticketUiState.tryEmit(TicketsUiState.Success(ticketingProvider.fetchTickets()))
        } catch (throwable: Throwable) {
            _ticketUiState.tryEmit(TicketsUiState.Error(throwable))
        }
    }

    suspend fun logout() {
        withContext(viewModelScope.coroutineContext + backgroundCoroutineContext) {
            ticketingProvider.logout()
        }
    }
}

internal sealed class TicketsUiState {
    data class Success(val tickets: List<Ticket>?) : TicketsUiState()
    data class Error(val throwable: Throwable) : TicketsUiState()
}