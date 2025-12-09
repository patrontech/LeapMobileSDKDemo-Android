package com.greencopper.interfacekit.textstyle.subsystem

import android.graphics.Typeface
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.core.MockAssetsStorageManager
import com.greencopper.testmocks.core.MockImageDirectory
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertDoesNotThrow

internal class ConcreteTextStyleRepositoryTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val fontsJson: String =
        context.assets.open("testContent/fonts/fontsOverride-config.json").bufferedReader()
            .use { it.readText() }

    private val textStyleRepository: ConcreteTextStyleRepository
    private val assetsStorageManager =
        MockAssetsStorageManager(emptyList(), MockImageDirectory(arrayListOf()), androidAssetManager = context.assets)
    private val spyCachedTypefaces: MutableMap<String, Typeface> = mutableMapOf()

    init {
        Toolkit.setupTest(applicationContext = context)
        textStyleRepository = spyk(
            ConcreteTextStyleRepository(assetsStorageManager),
            recordPrivateCalls = true
        )
        every { textStyleRepository getProperty ("cachedTypefaces") } returns spyCachedTypefaces
    }

    @Nested
    @DisplayName("No config loaded")
    inner class NoConfigLoaded {

        @Test
        fun loadTextStyles_withoutDefaultTheme_shouldThrow() {
            val empty = TextStyleConfiguration.TextStyle(emptyList())
            val themeBackup = TextStyleConfiguration.Theme(
                TextStyleConfiguration.DefaultSet(
                    largeTitle = empty,
                    title = TextStyleConfiguration.Title(empty, empty, empty, empty, empty),
                    headline = TextStyleConfiguration.Headline(empty, empty, empty),
                    body = TextStyleConfiguration.Body(empty, empty, empty, empty, empty),
                    caption = TextStyleConfiguration.Caption(empty, empty),
                    footnote = TextStyleConfiguration.Footnote(empty, empty)
                ), null
            )
            assertThrows<IllegalStateException> {
                textStyleRepository.loadTextStyles(
                    TextStyleConfiguration(
                        mapOf("foo" to themeBackup)
                    )
                )
            }
        }

        @Test
        fun getIKFont_whenNoConfigLoaded_shouldNotCrash() {
            assertDoesNotThrow {
                textStyleRepository.getIKFont(listOf("foo"), IKFont.TextStyle.bodyM)
                assertThat(spyCachedTypefaces).containsKey("bodyM")
            }
        }
    }

    @Nested
    @DisplayName("With config loaded")
    inner class WithConfigLoaded {
        init {
            textStyleRepository.loadTextStyles(KiboSerializable.decodeFromString(fontsJson))
        }

        @Test
        fun loadTextStyles_withValidConfig_shouldLoad() {
            textStyleRepository.getIKFont(listOf("foo"), IKFont.TextStyle.largeTitle)
            assertThat(spyCachedTypefaces).containsKey("inter_bold.ttf")
        }

        @Test
        fun whenGettingFont_withOverrideAvailable_shouldGetOverride() {
            textStyleRepository.getIKFont(listOf("interfaceKit", "sample", "largeTitle"))
            assertThat(spyCachedTypefaces).containsKey("Bonjour.otf")
        }

        @Test
        fun whenGettingFont_withTypefaceAlreadyCached_shouldGetCachedTypeface() {
            val font1 = textStyleRepository.getIKFont(listOf("interfaceKit", "sample", "largeTitle"))
            val font2 = textStyleRepository.getIKFont(listOf("interfaceKit", "sample", "largeTitle"))
            assertThat(spyCachedTypefaces).containsKey("Bonjour.otf")
            assertThat(font1.typeface).isSameAs(font2.typeface)
        }

        @Test
        fun whenGettingFont_withUnknownLevel_withFallback_shouldGetFallback() {
            val knownIKFont = textStyleRepository.getIKFont(listOf("interfaceKit", "sample", "largeTitle"))

            val unknownIKFont = textStyleRepository.getIKFont(
                listOf("foo"),
                IKFont.TextStyle.bodyM,
                knownIKFont
            )
            assertThat(unknownIKFont.typeface).isSameAs(knownIKFont.typeface)
        }

        @Test
        fun whenGettingFont_withBrokenFontAsset_shouldGetDefaultTypeface() {
            val ikFontWithDefaultTypeface = textStyleRepository.getIKFont(listOf("foo"), IKFont.TextStyle.bodyM)
            val ikFontWithBrokenFont = textStyleRepository.getIKFont(listOf("bar"), IKFont.TextStyle.titleXS)

            assertThat(ikFontWithDefaultTypeface.typeface).isNotSameAs(ikFontWithBrokenFont.typeface)
            assertThat(ikFontWithBrokenFont.typeface).isSameAs(IKFont.TextStyle.titleXS.fallbackFont)
        }
    }
}
