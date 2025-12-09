package com.greencopper.ticketing.providers.showclix.repository

import android.util.Base64
import com.greencopper.coremocks.SignatureGeneratorMock
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketing.mocks.MockShowclixAPI
import com.greencopper.ticketing.providers.showclix.data.ShowclixFetchedTickets
import com.greencopper.ticketing.providers.showclix.data.VerifyTokenData
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class ConcreteShowclixMemberRepositoryTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
        mockkStatic("android.util.Base64")
        every { Base64.encodeToString(any(), any()) } returns "authKey"
    }

    private val showclixAPI = MockShowclixAPI()
    private val remoteStateDispatcher = MockRemoteStateDispatcher(json = App.resolve())
    private val signatureGenerator = SignatureGeneratorMock()

    private val url = "https://www.test.com/"
    private val email = "email"
    private val magicLink = "magicLink"
    private val token = "token"

    private val showclixMemberRepository = ConcreteShowclixMemberRepository(
        showclixAPI,
        signatureGenerator,
        "apiKey",
        remoteStateDispatcher,
        testScope,
        App.resolve(),
    )

    override fun afterEach() {}

    @Test
    fun sendMagicLink_withRequestReturningIncorrectResponse_shouldReturnFalse() {
        showclixAPI.getMagicLinkUrlRequest = { throw Exception() }
        runTest {
            val result = showclixMemberRepository.sendMagicLink(url, email, magicLink)
            assertThat(result).isFalse
        }
    }

    @Test
    fun sendMagicLink_withRequestReturningCorrectResponse_shouldReturnTrue() {
        runTest {
            val result = showclixMemberRepository.sendMagicLink(url, email, magicLink)
            assertThat(result).isTrue
            assertThat(showclixAPI.getMagicLinkUrlCount).isEqualTo(1)
        }
    }

    @Test
    fun verifyToken_withRequestReturningIncorrectResponse_shouldReturnNull() {
        runTest {
            val result = showclixMemberRepository.verifyToken(url, token)
            assertThat(result).isNull()
            assertThat(showclixAPI.verifyTokenCount).isEqualTo(1)
        }
    }

    @Test
    fun verifyToken_withRequestReturningCorrectResponse_shouldReturnData() {
        val verifyToken = VerifyTokenData(
            VerifyTokenData.Data(
                "id123",
                VerifyTokenData.Data.Attributes(
                    "email",
                    "token"
                )
            )
        )

        showclixAPI.verifyTokenRequest = { verifyToken }
        runTest {
            val result = showclixMemberRepository.verifyToken(url, token)
            assertThat(result).isNotNull
            assertThat(result?.data?.id).isEqualTo(verifyToken.data.id)
        }
    }

    @Test
    fun fetchTickets_withRequestReturningCorrectResponse_shouldReturnFilteredData() {
        val tickets = ShowclixFetchedTickets(
            listOf(
                ShowclixFetchedTickets.Ticket(
                    "name1",
                    "id1",
                    true,
                    "2022-11-22T20:24:00-04:00",
                ),
                ShowclixFetchedTickets.Ticket(
                    "name",
                    "id2",
                    false,
                    "2022-11-22T20:24:00-04:00",
                )
            )
        )

        val testDate = ZonedDateTime.parse("2022-11-22T20:24:00-04:00",
            DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        showclixAPI.getTicketsRequest = { tickets }
        runTest {
            val result = showclixMemberRepository.fetchTickets(url)
            assertThat(result).isNotNull
            assertThat(result.size).isEqualTo(1)
            assertThat(result[0].qrCode).isEqualTo(tickets.tickets[1].id)
            assertThat(result[0].startDate?.toInstant()
                ?.toEpochMilli()).isEqualTo(testDate.toInstant().toEpochMilli())
        }
    }

    @Test
    fun fetchTickets_withRequestReturningCorrectResponse_withMalformedDate_shouldReturnFilteredData_withNullDate() {
        val tickets = ShowclixFetchedTickets(
            listOf(
                ShowclixFetchedTickets.Ticket(
                    "name1",
                    "id1",
                    true,
                ),
                ShowclixFetchedTickets.Ticket(
                    "name2",
                    "id2",
                    false,
                    "2022-11-22",
                ),
                ShowclixFetchedTickets.Ticket(
                    "name3",
                    "id3",
                    false,
                    "date",
                )
            )
        )

        showclixAPI.getTicketsRequest = { tickets }
        runTest {
            val result = showclixMemberRepository.fetchTickets(url)
            assertThat(result).isNotNull
            assertThat(result.size).isEqualTo(2)
            assertThat(result[0].qrCode).isEqualTo(tickets.tickets[1].id)
            assertThat(result[0].startDate).isNull()
            assertThat(result[1].startDate).isNull()
        }
    }

    @Nested
    @DisplayName("Exception thrown when doing HTTP request")
    inner class HTTPRequestException {
        init {
            showclixAPI.getMagicLinkUrlRequest = { throw HttpException(Response.error<String>(500, "".toResponseBody())) }
            showclixAPI.verifyTokenRequest = { throw HttpException(Response.error<String>(500, "".toResponseBody())) }
            showclixAPI.getTicketsRequest = { throw HttpException(Response.error<String>(500, "".toResponseBody())) }
        }

        @Test
        fun sendMagicLink_shouldReturnFalse() {
            runTest {
                val result = showclixMemberRepository.sendMagicLink(url, email, magicLink)
                assertThat(result).isFalse()
            }
        }

        @Test
        fun verifyToken_shouldReturnNull() {
            runTest {
                val result = showclixMemberRepository.verifyToken(url, token)
                assertThat(result).isNull()

            }
        }

        @Test
        fun fetchTickets_shouldThrow() {
            runTest {
                assertThrows<HttpException> {
                    showclixMemberRepository.fetchTickets(url)
                }
            }
        }
    }
}
