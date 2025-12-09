package com.greencopper.interfacekit.ui

import android.util.Xml
import android.view.InflateException
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.ui.views.navigationcontrols.NavigateBackButton
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import io.mockk.every
import io.mockk.mockkClass
import org.junit.jupiter.api.*
import org.xmlpull.v1.XmlPullParser

internal class NavigateBackButtonTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val materialContext = ContextThemeWrapper(context, androidx.appcompat.R.style.Base_Theme_AppCompat)
    private val topBarColor = mockkClass(TopBarColor::class)
    private val fragmentActivity = mockkClass(FragmentActivity::class)

    @Test
    fun initWithContext_withoutAppCompatTheme_ShouldThrow() {
        assertThrows<InflateException> { NavigateBackButton(context) }
    }

    @Test
    fun initWithContext_withAppCompatTheme_ShouldSuccess() {
        Assertions.assertNotNull(NavigateBackButton(materialContext))
    }

    @Test
    fun initWithContextAttributes_shouldNotFail() {
        val parser: XmlPullParser = context.resources.getXml(android.R.layout.test_list_item)
        val attr = Xml.asAttributeSet(parser)
        Assertions.assertNotNull(NavigateBackButton(materialContext, attr))
    }

    @Test
    fun initWithContextAttributesStyleAttributes_shouldNotFail() {
        val parser: XmlPullParser = context.resources.getXml(android.R.layout.test_list_item)
        val attr = Xml.asAttributeSet(parser)
        Assertions.assertNotNull(NavigateBackButton(materialContext, attr, -1))
    }

    @Test
    fun setUpButton_shouldNotThrow() {
        every { topBarColor.background } returns 0
        every { topBarColor.item } returns 0
        Assertions.assertDoesNotThrow { NavigateBackButton(materialContext).setupButton(topBarColor, NavigationControlsHandler.DefaultBackPressedListener(fragmentActivity)) }
    }

    @Test
    fun afterSetUpButton_clickingOnBackShouldCallOnBackPressed() {
        var onBackPressedHasBeenCalled = false
        every { topBarColor.background } returns 0
        every { topBarColor.item } returns 0
        every { fragmentActivity.onBackPressedDispatcher.onBackPressed() } answers { onBackPressedHasBeenCalled = true }
        val backButton = NavigateBackButton(materialContext)
        assert(!onBackPressedHasBeenCalled)
        Assertions.assertDoesNotThrow { backButton.setupButton(topBarColor, NavigationControlsHandler.DefaultBackPressedListener(fragmentActivity)) }
        assert(!onBackPressedHasBeenCalled)
        backButton.binding.navigateFab.callOnClick()
        assert(onBackPressedHasBeenCalled)
    }
}
