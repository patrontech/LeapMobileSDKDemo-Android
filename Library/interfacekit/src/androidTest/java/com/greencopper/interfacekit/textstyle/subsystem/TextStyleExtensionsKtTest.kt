package com.greencopper.interfacekit.textstyle.subsystem

import android.widget.Button
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TextStyleExtensionsKtTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    private val textStyleRepository = MockTextStyleRepository()

    init {
        Toolkit.setupTest()
        bindSingleton<TextStyleRepository>(textStyleRepository)
    }

    @Test
    fun textViewExtension_shouldApplyChanges() {
        //given
        val textView = TextView(context)
        val previousTypeface = textView.typeface
        val previousTextSize = textView.textSize

        //when
        textView.setFont(IKFont(IKFont.TextStyle.largeTitle))

        //then
        assertThat(previousTypeface).isNotEqualTo(textView.typeface)
        assertThat(previousTextSize).isNotEqualTo(textView.textSize)
    }

    @Test
    fun buttonExtension_shouldApplyChanges() {
        //given
        val button = Button(context)
        val previousTypeface = button.typeface
        val previousTextSize = button.textSize

        //when
        button.setFont(IKFont(IKFont.TextStyle.largeTitle))

        //then
        assertThat(previousTypeface).isNotEqualTo(button.typeface)
        assertThat(previousTextSize).isNotEqualTo(button.textSize)
    }

}
