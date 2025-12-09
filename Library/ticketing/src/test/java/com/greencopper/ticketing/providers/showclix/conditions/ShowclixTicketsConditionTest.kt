package com.greencopper.ticketing.providers.showclix.conditions

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.ticketing.models.Ticket
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ShowclixTicketsConditionTest {

    private val showclixTicketsCondition: ShowclixTicketsCondition
    private val lazyLocalStorage: LazyResolver<LocalStorage>

    private val date = ZonedDateTime.of(2022, 11, 22, 20, 24, 30, 0, ZoneId.systemDefault())
    private val ticket = Ticket(
        primaryTitle = "testname",
        primarySubtitle = "subtitle",
        qrCode = "1234",
        secondaryTitle = "secondary",
        startDate = date,
    )

    init {
        Toolkit.setupTest()
        bindProvider<TimezoneProvider>(MockTimezoneProvider())
        lazyLocalStorage = LazyResolver.adhoc(LocalStorage("project"))
        showclixTicketsCondition = ShowclixTicketsCondition(lazyLocalStorage)

        lazyLocalStorage.resolve().project.showclix.tickets.value = listOf(ticket)
    }

    @Test
    fun checkWithFlow_shouldSucceed() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "2022-11-22")

        runTest {
            val result = showclixTicketsCondition.checkWithFlow(params).first()
            assertThat(result).isTrue
        }
    }

    @Test
    fun whenConditionDateIsValid_shouldSucceed() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "2022-11-22")

        assertThat(showclixTicketsCondition.checkWith(params)).isTrue
    }

    @Test
    fun whenConditionDateIsDifferent_shouldFail() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "2022-11-01")

        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenConditionDateAndNameIsValid_shouldSucceed() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "2022-11-22",
            name = "testname")

        assertThat(showclixTicketsCondition.checkWith(params)).isTrue
    }

    @Test
    fun whenConditionNameIsInvalid_shouldFail() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(name = "pouet")

        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenConditionBetweenIsValid_shouldSucceed() {
        var params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "2022-11-23",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isTrue

        params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-22",
                date2 = "2022-11-23",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isTrue

        params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "2022-11-22",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isTrue
    }

    @Test
    fun whenConditionBetweenIsDifferent_shouldFail() {
        var params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-10",
                date2 = "2022-11-15",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse

        params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-25",
                date2 = "2022-11-30",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse

        params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-23",
                date2 = "2022-11-21",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenConditionBetweenIsMalformed_shouldFail() {
        var params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "123",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse

        params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "123",
                date2 = "2022-11-23",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenConditionDateAndBetweenIsPresent_shouldFail() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            date = "2022-11-22",
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "2022-11-23",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenConditionEmpty_shouldFail() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter()

        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenConditionDateIsMalformed_shouldFail() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "123")

        assertThat(showclixTicketsCondition.checkWith(params)).isTrue
    }

    @Test
    fun whenNoTickets_shouldFail() {
        lazyLocalStorage.resolve().project.showclix.tickets.value = emptyList()
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "2022-11-22")

        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun whenTicketDateIsNull_shouldFail() {
        lazyLocalStorage.resolve().project.showclix.tickets.value = listOf(
            Ticket(
                primaryTitle = "testname",
                primarySubtitle = "subtitle",
                qrCode = "1234",
                secondaryTitle = "secondary",
                startDate = null,
            )
        )

        var params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(date = "2022-11-22")
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse

        params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "2022-11-23",
            )
        )
        assertThat(showclixTicketsCondition.checkWith(params)).isFalse
    }

    @Test
    fun testDeserialization() {
        val params = ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            date = "2022-11-22",
            name = "name",
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "2022-11-23",
            ),
        )
        val deserialized = showclixTicketsCondition.deserialize(params.encodeToJsonElement())

        assertThat(deserialized).isEqualTo(params)
    }

    @Test
    fun testParamsSerialization() {
        testKiboSerializable(ShowclixTicketsCondition.ShowclixTicketsConditionParameter(
            date = "2022-11-22",
            name = "name",
            between = ShowclixTicketsCondition.ShowclixTicketsConditionParameter.Between(
                date1 = "2022-11-21",
                date2 = "2022-11-23",
            ),
        ))
    }
}
