package com.greencopper.ticketing.ticketsscan

import com.greencopper.testmocks.CoroutineTest
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketingmocks.providers.MockProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TicketsScanViewModelTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val provider = MockProvider()
    private val viewModel = TicketsScanViewModel(provider, testScope.coroutineContext)

    @Test
    fun getCachedTickets_returnsTicekts() {
        val tickets = listOf(
            Ticket("title", "subtitle", "qrCode", "secondaryTitle"),
            Ticket("title2", "subtitle2", "qrCode2", "secondaryTitle2"),
        )
        provider.cachedTicketsMock = tickets

        val result = viewModel.getCacheTickets()

        assertThat(result).isEqualTo(tickets)
    }

    @Test
    fun noError_fetchTickets_emitsSuccessState() {
        viewModel.fetchTickets()
        runTest {
            val state = viewModel.ticketUiState.first()
            assertThat(state).isInstanceOf(TicketsUiState.Success::class.java)
        }
    }

    @Test
    fun withError_fetchTickets_emitsErrorState() {
        provider.fetchTicketsFun = { throw Exception() }
        viewModel.fetchTickets()

        runTest {
            val state = viewModel.ticketUiState.first()
            assertThat(state).isInstanceOf(TicketsUiState.Error::class.java)
        }
    }

    @Test
    fun logout() {
        var logoutCalled = false
        provider.logoutFun = {
            logoutCalled = true
        }

        runTest {
            viewModel.logout()
            assertThat(logoutCalled).isTrue
        }
    }

    override fun afterEach() {}
}
