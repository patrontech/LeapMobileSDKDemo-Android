package com.greencopper.ticketing.providers.showclix

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.ProviderException
import com.greencopper.ticketing.providers.ProviderInfo
import com.greencopper.ticketingmocks.providers.showclix.repository.MockShowclixMemberRepository
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response

internal class ShowclixProviderTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val showclixMemberRepository = MockShowclixMemberRepository()
    private val showclixProvider = ShowclixProvider(
        showclixMemberRepository,
        localStorage,
    )

    @BeforeEach
    fun beforeEach() {
        showclixProvider.setup(ShowclixProvider.Data("apiUrl"))
    }

    override fun afterEach() {}

    @Test
    fun verifyKey() {
        assertThat(ShowclixProvider.key).isEqualTo(
            ProviderInfo.Key(
                "Ticketing.Provider.Showclix",
                1
            )
        )
    }

    @Test
    fun verifyDeserializeFunction() {
        val data = ShowclixProvider.Data("apiUrl")
        val deserializedParams = showclixProvider.deserialize(data.encodeToJsonElement())

        assertThat(deserializedParams.apiUrl).isEqualTo(data.apiUrl)
    }

    @Test
    fun setup_withNullParams_shouldThrow() {
        assertThrows<ProviderException.ParamsRequiredException> {
            showclixProvider.setup(null)
        }
    }

    @Test
    fun whenCallingLogout_shouldCleanLocalStorage() {
        //given
        localStorage.project.showclix.validationToken.value = "123"

        //when
        runTest {
            showclixProvider.logout()
        }

        //then
        assertThat(localStorage.project.showclix.validationToken.value).isNull()
    }

    @Test
    fun getCachedTickets_shouldReturnData() {
        //given
        localStorage.project.showclix.tickets.value = listOf(
            Ticket(
                primaryTitle = "title",
                primarySubtitle = null,
                qrCode = "123456",
                secondaryTitle = null
            )
        )
        var result: List<Ticket> = emptyList()

        //when
        runTest {
            result = showclixProvider.cachedTickets
        }

        //then
        assertThat(result[0].primaryTitle).isEqualTo("title")
    }

    @Test
    fun fetchTickets_with401Exception_shouldThrow() {
        //given
        showclixMemberRepository.fetchTicketsFun =
            { throw HttpException(Response.error<String>(401, "".toResponseBody())) }

        //then
        runTest {
            assertThrows<ProviderException.TokenExpiredException> {
                showclixProvider.fetchTickets()
            }
        }
    }

    @Test
    fun fetchTickets_withException_shouldThrow() {
        //given
        showclixMemberRepository.fetchTicketsFun =
            { throw RuntimeException() }

        //then
        runTest {
            assertThrows<ProviderException> {
                showclixProvider.fetchTickets()
            }
        }
    }

    @Test
    fun fetchTickets_shouldReturnData_andSaveCache() {
        //given
        showclixMemberRepository.fetchTicketsFun = {
            listOf(
                Ticket(
                    primaryTitle = "title",
                    primarySubtitle = null,
                    qrCode = "123456",
                    secondaryTitle = null
                )
            )
        }
        var result: List<Ticket> = emptyList()

        //when
        runTest {
            result = showclixProvider.fetchTickets()
        }

        //then
        assertThat(result[0].primaryTitle).isEqualTo("title")
        assertThat(localStorage.project.showclix.tickets.value[0].primaryTitle).isEqualTo("title")
    }

}
