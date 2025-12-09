package com.greencopper.interfacekit.color

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.core.graphics.toColorInt
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.color.repository.ConcreteColorRepository
import com.greencopper.interfacekit.ui.compose.mockColors
import com.greencopper.testmocks.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import de.mannodermaus.junit5.compose.createComposeExtension
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.RegisterExtension
import androidx.compose.ui.graphics.Color as ColorCompose
import com.greencopper.interfacekit.color.Color as ColorKIBA

internal class UIColorTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val colorJson: String =
        context.assets.open("colors/colorsOverride-config.json").bufferedReader()
            .use { it.readText() }

    private class UIColorTestImpl(levelsValue: String) : UIColor() {
        override val level: String = levelsValue
    }

    init {
        mockkStatic("com.greencopper.interfacekit.color.UIColorKt")
        every { isDarkMode() } returns false
        Toolkit.setupTest(applicationContext = context)
        val colorRepository = ConcreteColorRepository()
        colorRepository.loadColors(
            App.resolve<Json>().decodeFromString(
                ColorsConfiguration.serializer(),
                colorJson
            )
        )
        bindSingleton<ColorRepository>(colorRepository)
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic("com.greencopper.interfacekit.color.UIColorKt")
    }

    @Test
    fun whenGettingColor_withOverride_getCustomString() {
        val colorKey = InterfaceKitColor.bottomBar.background
        val defaultKey = UIColor.default.background.secondary.light
        assertThat(colorKey).isNotEqualTo(defaultKey)
        assertThat(colorKey).isEqualTo("#ff9c9c9c".toColorInt())
    }

    @Test
    fun whenGettingColor_withoutOverride_getDefaultString() {
        val colorKey = InterfaceKitColor.sample.icon
        val defaultKey = UIColor.default.fill.primary.light
        assertThat(colorKey).isEqualTo(defaultKey)
    }

    @Test
    fun whenGettingColorStyle_withOverride_getCustomString() {
        val colorStyle = InterfaceKitColor.sample.statusBar
        val defaultColorStyle = UIColor.default.statusBar
        assertThat(colorStyle).isNotEqualTo(defaultColorStyle)
    }

    @Test
    fun whenGettingColorStyle_withoutOverride_getDefaultString() {
        val colorStyle = InterfaceKitColor.webView.statusBar
        val defaultColorStyle = UIColor.default.statusBar
        assertThat(colorStyle).isEqualTo(defaultColorStyle)
    }

    @Test
    fun whenRemovingAlpha_shouldRemoveAlpha() {
        val alphaColor = Color.argb(50, 125, 125, 125)
        val noAlpha = Color.rgb(125, 125, 125)
        assertThat(alphaColor).isNotEqualTo(noAlpha)
        assertThat(alphaColor.removeAlpha()).isEqualTo(noAlpha)
    }

    @Test
    fun accessingUIColorLevel() {
        val levelsValue = "levelsFromUIColorTestImpl"
        val uiColorInstance = UIColorTestImpl(levelsValue)
        assert(uiColorInstance.getLevels(levelsValue).contains(levelsValue))
    }

    @Test
    fun gettingOnboardingOverrideColorsShouldSucceed() {
        val colorStyle = InterfaceKitColor.mainActionCardOnboardingPage.statusBar
        val overrideColorStyle = DefaultColors.StatusBar(
            DefaultColors.StatusBar.Style.LIGHT,
            DefaultColors.StatusBar.Style.LIGHT
        )
        assertThat(colorStyle).isEqualTo(overrideColorStyle)

        val backgroundColor = InterfaceKitColor.mainActionCardOnboardingPage.background
        assertThat(backgroundColor).isEqualTo("#9c9c9cff".parseToColor())

        val cardTitleColor = InterfaceKitColor.mainActionCardOnboardingPage.card.title
        assertThat(cardTitleColor).isEqualTo("#272727FF".parseToColor())

        val cardButtonTextColor = InterfaceKitColor.mainActionCardOnboardingPage.card.button.text
        assertThat(cardButtonTextColor).isEqualTo("#FFFFFF33".parseToColor())

        val skipButtonColor = InterfaceKitColor.mainActionCardOnboardingPage.card.skip
        assertThat(skipButtonColor).isEqualTo("#00000040".parseToColor())
    }

    @Test
    fun gettingMultiProjectSwitcherOverrideColorsShouldSucceed() {
        val colorStyle = InterfaceKitColor.projectSwitcher.statusBar
        val overrideColorStyle = DefaultColors.StatusBar(
            DefaultColors.StatusBar.Style.LIGHT,
            DefaultColors.StatusBar.Style.LIGHT
        )
        assertThat(colorStyle).isEqualTo(overrideColorStyle)

        val titleColor = InterfaceKitColor.projectSwitcher.title
        assertThat(titleColor).isEqualTo("9a9c9c".parseToColor())

        val subtitleColor = InterfaceKitColor.projectSwitcher.subtitle
        assertThat(subtitleColor).isEqualTo("9b9c9c".parseToColor())

        val backgroundColor = InterfaceKitColor.projectSwitcher.background
        assertThat(backgroundColor).isEqualTo("9c9c9c".parseToColor())

        val swipeIndicatorColor = InterfaceKitColor.projectSwitcher.swipeIndicator
        assertThat(swipeIndicatorColor).isEqualTo("#454545FF".parseToColor())

        val footerTopShadowColor = InterfaceKitColor.projectSwitcher.footerTopShadow
        assertThat(footerTopShadowColor).isEqualTo("#565656FF".parseToColor())

        val buttonTitleColor = InterfaceKitColor.projectSwitcher.continueButton.text
        assertThat(buttonTitleColor).isEqualTo("#272727FF".parseToColor())

        val buttonBackgroundColor = InterfaceKitColor.projectSwitcher.continueButton.background
        assertThat(buttonBackgroundColor).isEqualTo("#FFFFFF33".parseToColor())

        val buttonBorderColor = InterfaceKitColor.projectSwitcher.continueButton.border
        assertThat(buttonBorderColor).isEqualTo("#787878FF".parseToColor())

        val projectBackgroundColor = InterfaceKitColor.projectSwitcher.project.background
        assertThat(projectBackgroundColor).isEqualTo("#00000040".parseToColor())

        val projectTitleColor = InterfaceKitColor.projectSwitcher.project.title
        assertThat(projectTitleColor).isEqualTo("#00000340".parseToColor())

        val projectSubtitleColor = InterfaceKitColor.projectSwitcher.project.subtitle
        assertThat(projectSubtitleColor).isEqualTo("#00000440".parseToColor())

        val projectSeparatorColor = InterfaceKitColor.projectSwitcher.project.separator
        assertThat(projectSeparatorColor).isEqualTo("#00000540".parseToColor())

        val projectCheckboxColor = InterfaceKitColor.projectSwitcher.project.checkbox
        assertThat(projectCheckboxColor).isEqualTo("#00000640".parseToColor())
    }

    @Test
    fun withLightSetup_DarkMode_shouldBeFalse() {
        unmockkStatic("com.greencopper.interfacekit.color.UIColorKt")
        val context = App.resolve<Context>()
        val newContext = context.createConfigurationContext(
            getNightConfiguration(
                light = true,
                resources = context.resources
            )
        )
        bindProvider(newContext)

        assertThat(isDarkMode()).isFalse
    }

    @Test
    fun withDarkSetup_DarkMode_shouldBeTrue() {
        unmockkStatic("com.greencopper.interfacekit.color.UIColorKt")
        val context = App.resolve<Context>()
        val newContext = context.createConfigurationContext(
            getNightConfiguration(
                light = false,
                resources = context.resources
            )
        )
        bindProvider(newContext)

        assertThat(isDarkMode()).isTrue
    }

    private fun getNightConfiguration(light: Boolean, resources: Resources): Configuration {
        val newConfiguration = Configuration(resources.configuration)
        val uiNightMode =
            if (light) Configuration.UI_MODE_NIGHT_NO else Configuration.UI_MODE_NIGHT_YES
        newConfiguration.uiMode =
            (uiNightMode or (newConfiguration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()))
        return newConfiguration
    }

    @Test
    fun whenParseRGBA_withValidColor_shouldReturnInt() {
        val testColor = "#3a3a3a80"
        val expectedColor = Color.parseColor("#803a3a3a")
        assertThat(testColor.parseRGBA()).isEqualTo(expectedColor)
    }

    @Test
    fun whenParseRGBA_withInValidColor_shouldThrow() {
        assertThrows<IllegalArgumentException> {
            runTest {
                val testColor = "a3a3a80"
                testColor.parseRGBA()
            }
        }
    }
}

@OptIn(ExperimentalTestApi::class)
@Nested
@Disabled
internal class UIColorComposeTests {
    @JvmField
    @RegisterExtension
    @ExperimentalTestApi
    val extension = createComposeExtension()

    @Test
    fun whenGettingInterfaceKitColor_shouldBuildCorrectly() {
        extension.use {
            setContent {
                CompositionLocalProvider(
                    mockColors(
                        mapOf(
                            listOf("interfaceKit", "test") to ColorCompose.Red,
                        )
                    )
                ) {
                    val colorCompose = InterfaceKitColor.composeColor(leaf = "test") { ColorKIBA(Color.RED, null) }
                    colorCompose shouldBe ColorCompose.Red
                }
            }
        }
    }
}
