package com.greencopper.toolkit.appinstance

import android.content.Context
import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.toolkit.App
import com.greencopper.toolkit.locale.toList
import com.greencopper.toolkit.locale.toLocaleList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

internal class ConcreteAppInstanceTestInstrumented {

    private val context: Context = InstrumentationRegistry.getInstrumentation().context
    private val testAssembly = TestAssembly()

    @Nested
    @DisplayName("When 'GERMANY, FRANCE, ITALY' device system config")
    inner class TestDeviceConfig1 {
        private val localesConfig = listOf(Locale.GERMANY, Locale.FRANCE, Locale.ITALY)

        @BeforeEach
        fun setup() {
            config.setLocales(localesConfig.toLocaleList())
            val app = ConcreteAppInstance.create(listOf(testAssembly), emptyList(), context)
            App = app
            app.assemble()
        }

        @Test
        fun whenGettingLocale_withoutChanges_shouldGetDeviceLocale() {
            assertThat(App.locale).isEqualTo(Locale.GERMANY)
        }

        @Test
        fun whenSettingForcedLocale_shouldGetForcedLocale() {
            App.setForcedLocale(Locale.TAIWAN)

            assertThat(App.locale).isEqualTo(Locale.TAIWAN)
            assertThat(getConfigLocales()).isEqualTo(listOf(Locale.TAIWAN) + localesConfig)
        }

        @Test
        fun whenSettingForcedLocale_whichAlreadyExists_shouldGetForcedLocale() {
            App.setForcedLocale(Locale.ITALY)

            assertThat(App.locale).isEqualTo(Locale.ITALY)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.ITALY,
                    Locale.GERMANY,
                    Locale.FRANCE
                )
            )
        }

        @Test
        fun whenSettingConfigLocale_withMatchingLocale_shouldSetMatching() {
            App.setConfigLocale(listOf(Locale.TAIWAN, Locale.ITALY), Locale.GERMANY)

            assertThat(App.locale).isEqualTo(Locale.ITALY)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.ITALY,
                    Locale.GERMANY,
                    Locale.FRANCE
                )
            )
        }

        @Test
        fun whenSettingConfigLocale_withNoMatchingLocale_shouldSetFallback() {
            App.setConfigLocale(listOf(Locale.TAIWAN), Locale.CHINA)

            assertThat(App.locale).isEqualTo(Locale.CHINA)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.CHINA,
                    Locale.GERMANY,
                    Locale.FRANCE,
                    Locale.ITALY
                )
            )
        }

        @Test
        fun whenSettingConfigLocale_withMatchingLanguage_shouldSetMatching() {
            App.setConfigLocale(listOf(Locale.TAIWAN, Locale.CANADA_FRENCH), Locale.CHINA)

            assertThat(App.locale).isEqualTo(Locale.FRANCE)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.FRANCE,
                    Locale.GERMANY,
                    Locale.ITALY
                )
            )
        }

        @Test
        fun whenSettingConfigLocale_shouldSelectDeviceSettingsLocaleFirst() {
            // listOf(Locale.GERMANY, Locale.FRANCE, Locale.ITALY)
            // FRANCE has more priority, cause it's before ITALY in device settings

            // setup CMS locales
            App.setConfigLocale(listOf(Locale.ITALY, Locale.FRANCE), Locale.CHINA)

            assertThat(App.locale).isEqualTo(Locale.FRANCE)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.FRANCE,
                    Locale.GERMANY,
                    Locale.ITALY
                )
            )
        }

        @Test
        fun whenSettingConfigLocale_shouldSelectDeviceSettingsLocaleFirst_ByLanguage() {
            // listOf(Locale.GERMANY, Locale.FRANCE, Locale.ITALY)
            // CANADA_FRENCH has more priority, cause FRANCE before ITALY in device settings

            // setup CMS locales
            App.setConfigLocale(listOf(Locale.ITALY, Locale.CANADA_FRENCH), Locale.CHINA)

            assertThat(App.locale).isEqualTo(Locale.FRANCE)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.FRANCE,
                    Locale.GERMANY,
                    Locale.ITALY
                )
            )
        }

        @Test
        fun whenSettingForcedLocale_andConfigLocale_shouldSetLocales() {
            App.setForcedLocale(Locale.CANADA)
            App.setConfigLocale(listOf(Locale.TAIWAN), Locale.CHINA)

            assertThat(App.locale).isEqualTo(Locale.CANADA)
            assertThat(getConfigLocales()).isEqualTo(
                listOf(
                    Locale.CANADA,
                    Locale.CHINA
                ) + localesConfig
            )
        }

        @Test
        fun whenResettingForcedLocale_getLocales() {
            App.setForcedLocale(Locale.CANADA)
            App.setConfigLocale(listOf(Locale.TAIWAN), Locale.CHINA)

            App.setForcedLocale(null)

            assertThat(App.locale).isEqualTo(Locale.CHINA)
            assertThat(getConfigLocales()).isEqualTo(listOf(Locale.CHINA) + localesConfig)
        }

        @Test
        fun whenResettingConfigLocale_getLocales() {
            App.setForcedLocale(Locale.CANADA)
            App.setConfigLocale(listOf(Locale.TAIWAN), Locale.CHINA)

            App.setConfigLocale(null, null)

            assertThat(App.locale).isEqualTo(Locale.CANADA)
            assertThat(getConfigLocales()).isEqualTo(listOf(Locale.CANADA) + localesConfig)
        }

        @Test
        fun whenLoadingTestAssembly_onBindingsRegisteredRuns() {
            assertThat(testAssembly.isRegistered).isTrue
        }
    }

    @Nested
    @DisplayName("When 'CANADA_FRENCH, FRENCH' device system config")
    inner class TestDeviceConfig2 {
        private val localesConfig = listOf(Locale.CANADA_FRENCH, Locale.FRANCE)

        @BeforeEach
        fun setup() {
            config.setLocales(localesConfig.toLocaleList())
            val app = ConcreteAppInstance.create(listOf(testAssembly), emptyList(), context)
            App = app
            app.assemble()
        }

        @Test
        fun whenSettingConfigLocale_shouldSelectDeviceSettingsLocaleFirst_ByCountry() {
            // device locales listOf(Locale.CANADA_FRENCH, Locale.FRANCE)
            // CANADA_FRENCH has more priority

            // CMS locales
            App.setConfigLocale(listOf(Locale.FRANCE, Locale.CANADA_FRENCH), Locale.CHINA)

            assertThat(App.locale).isEqualTo(Locale.CANADA_FRENCH)
            assertThat(getConfigLocales()).isEqualTo(listOf(Locale.CANADA_FRENCH, Locale.FRANCE))
        }
    }

    private val config: Configuration
        get() = context.resources.configuration

    private fun getConfigLocales(): List<Locale> =
        config.locales.toList()
}
