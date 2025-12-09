package com.greencopper.core.permissions

import android.content.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SettingsPanelConfigTest {

    @Test
    fun checkAttributes() {
        val title = "title"
        val message = "message"
        val positiveTextButton = "positiveTextButton"
        val negativeTextButton = "negativeTextButton"
        val intentToOpen = Intent()
        val config = SettingsPanelConfig(
            title = title,
            message = message,
            positiveButtonString = positiveTextButton,
            negativeButtonString = negativeTextButton,
            intentToOpen = intentToOpen
        )

        assertThat(config.title).isEqualTo(title)
        assertThat(config.message).isEqualTo(message)
        assertThat(config.positiveButtonString).isEqualTo(positiveTextButton)
        assertThat(config.negativeButtonString).isEqualTo(negativeTextButton)
        assertThat(config.intentToOpen).isEqualTo(intentToOpen)
    }

}