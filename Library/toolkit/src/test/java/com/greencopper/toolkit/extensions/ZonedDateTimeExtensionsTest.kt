package com.greencopper.toolkit.extensions

import com.greencopper.toolkit.App
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import java.util.Locale


internal class ZonedDateTimeExtensionsTest {

    private val dateTest = ZonedDateTime.parse("2021-01-22T07:00:00-05:00")

    init {
        App = mockk()
        every { App.locale } returns Locale.US
        every { App.zoneId } returns ZoneId.of("Canada/Eastern")
    }

    @Test
    fun format_full_date_time() {
        val dateString = dateTest.getFormattedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM, App.zoneId)
        assertThat(dateString).contains("Jan")
        assertThat(dateString).contains("22,")
        assertThat(dateString).contains("2021")
        assertThat(dateString).contains("7:00:00")
    }

    @Test
    fun format_date() {
        val dateString = dateTest.getFormattedDateTime(FormatStyle.MEDIUM, null, App.zoneId)
        assertThat(dateString).contains("Jan")
        assertThat(dateString).contains("22,")
        assertThat(dateString).contains("2021")
    }

    @Test
    fun format_time() {
        val dateString = dateTest.getFormattedDateTime(null, FormatStyle.MEDIUM, App.zoneId)
        assertThat(dateString).contains("7:00:00")
        assertThat(dateString).containsIgnoringCase("a")
    }

    @Test
    fun format_empty() {
        assertThrows<IllegalArgumentException> {
            dateTest.getFormattedDateTime(null, null, App.zoneId)
        }
    }

    @Test
    fun checkIsSameDay_whenEqual() {
        val today = ZonedDateTime.parse("2018-12-16T10:28:33.213+05:30[Asia/Calcutta]")
        val alsoToday = ZonedDateTime.parse("2018-12-16T20:28:33.213+05:30[Asia/Calcutta]")

        assertThat(today.isSameDayAs(alsoToday)).isTrue
    }

    @Test
    fun checkIsSameDay_whenNotEqual() {
        val today = ZonedDateTime.now()
        val notToday = ZonedDateTime.now().plusDays(1)

        assertThat(today.isSameDayAs(notToday)).isFalse
    }

    @Test
    fun isTomorrowForShouldReturnTrue() {
        val today = ZonedDateTime.now()
        val yesterday = ZonedDateTime.now().minusDays(1)

        assertThat(yesterday.isYesterdayFor(today)).isTrue
    }

    @Test
    fun isTomorrowForShouldReturnFalse() {
        val today = ZonedDateTime.now()
        val tomorrow = ZonedDateTime.now().plusDays(1)

        assertThat(tomorrow.isYesterdayFor(today)).isFalse
    }
}
