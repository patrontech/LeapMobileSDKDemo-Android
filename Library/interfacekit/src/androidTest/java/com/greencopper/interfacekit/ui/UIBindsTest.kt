package com.greencopper.interfacekit.ui

import android.view.View
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UIBindsKtTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    init {
        Toolkit.setupTest()
        bindProvider<LocalizationService>(MockLocalizationService())
    }

    @Test
    fun setOtaText() {
        val textView = TextView(context)
        textView.setOtaText("any_key")
        assertThat(textView.text).isNotBlank
    }

    @Test
    fun withText_setOtaTextOrGone_isVisible() {
        val textView = TextView(context)
        textView.setOtaTextOrGone(MockLocalizationService(), "any_key")
        assert(textView.visibility == View.VISIBLE)
    }

    @Test
    fun withoutText_setOtaTextOrGone_isGone() {
        val textView = TextView(context)
        textView.setOtaTextOrGone(MockLocalizationService(), null)
        assert(textView.visibility == View.GONE)
    }
}