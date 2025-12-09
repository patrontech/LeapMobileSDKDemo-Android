package com.greencopper.ticketing.metrics

import com.greencopper.core.metrics.Screen
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ScreenTest {

    @Test
    fun ticketsScan_shouldMatchValues() {
        val screen = Screen.ticketsScan("test")
        assertThat(screen.name).isEqualTo("test")
        assertThat(screen.klass).isEqualTo("ticketing_tickets_scan")
    }

    @Test
    fun showclixLogin_shouldMatchValues() {
        val screen = Screen.showclixLogin("test")
        assertThat(screen.name).isEqualTo("test")
        assertThat(screen.klass).isEqualTo("ticketing_showclix_login")
    }
}