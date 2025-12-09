package com.greencopper.core.permissions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RationalePanelConfigTest {

    @Test
    fun checkAttributes() {
        val title = "title"
        val message = "message"
        val textButton = "textButton"
        val config = RationalePanelConfig(
            title = title,
            message = message,
            positiveButtonString = textButton
        )

        assertThat(config.title).isEqualTo(title)
        assertThat(config.message).isEqualTo(message)
        assertThat(config.positiveButtonString).isEqualTo(textButton)
    }

}