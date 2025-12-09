package com.greencopper.core.localization.translation.wrapper

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.localization.service.*
import com.greencopper.core.localization.translation.LocalStorageTranslationRepository
import com.greencopper.core.localization.translation.TranslationRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.locale.toLocale
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class JsonLocalizationServiceTest {

    private var translationRepository: TranslationRepository
    private var localizationService: LocalizationService

    private val jsonTextDE = """{
                      "hello_message": "Hello",
                      "hello_friend":  {
                        "one": "Hello friend",
                        "other": "Hello friends"
                      }
                    }"""
    private val jsonTextJP = """{
                      "hello_message": "こんにちは"
                    }"""

    private val testKey = "hello_message"
    private val testValue = "Hello"

    init {
        val context = InstrumentationRegistry.getInstrumentation().context
        Toolkit.setupTest(applicationContext = context)

        translationRepository = LocalStorageTranslationRepository(App.resolve())
        localizationService = JsonLocalizationService(context, translationRepository)
        assertThat(translationRepository.getSupportedLocales()).isEmpty()
    }

    @AfterEach
    fun afterEach() {
        App.setForcedLocale(null)
    }

    @Test
    fun whenLoadingTranslations_shouldBeResolved() {
        translationRepository.setFallbackLocale("de-DE")
        var text = localizationService.getString(testKey)
        assertThat(text).isEqualTo(testKey)
        translationRepository.loadJsonMap(mapOf(App.locale to jsonTextDE))
        text = localizationService.getString(testKey)
        assertThat(text).isEqualTo(testValue)
    }

    @Test
    fun whenGettingString_shouldRetrieveString() {
        App.setForcedLocale(Locale.GERMANY)

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        val text = localizationService.getString(testKey)
        assertThat(text).isEqualTo(testValue)
    }

    @Test
    fun whenGettingNull_shouldReturnNull() {
        App.setForcedLocale(Locale.GERMANY)

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        val text = localizationService.getString(null)
        assertThat(text).isNull()
    }

    @Test
    fun whenGettingNullableString_shouldRetrieveString() {
        App.setForcedLocale(Locale.GERMANY)

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        val key = testKey
        val text = localizationService.getString(key)
        assertThat(text).isEqualTo(testValue)
    }

    @Test
    fun whenGettingNullableString_withWrongKey_shouldReturnKey() {
        App.setForcedLocale(Locale.GERMANY)
        translationRepository.setFallbackLocale("jp-JP")

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        val key = "notExistingKey"
        val text = localizationService.getString(key)
        assertThat(text).isEqualTo(key)
    }

    @Test
    fun whenGettingString_withDefault_shouldRetrieveString() {
        App.setForcedLocale(Locale.GERMANY)

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        val text = localizationService.getStringOrDefault(testKey, "pouet")
        assertThat(text).isEqualTo(testValue)
    }

    @Test
    fun whenGettingString_withSameLanguage_shouldRetrieveString() {
        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))

        // Same language
        App.setForcedLocale("de-CH".toLocale())
        val stringAUSameLanguage = localizationService.getString(testKey)
        assertThat(stringAUSameLanguage).isEqualTo(testValue)
    }

    @Test
    fun whenGettingString_withUnknownLocale_withFallback_shouldRetrieveFallback() {
        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        translationRepository.setFallbackLocale("ja-JP")

        App.setForcedLocale(Locale.JAPAN)
        // Different language no fallback
        val stringJPNoLocale = localizationService.getString(testKey)
        assertThat(stringJPNoLocale).isEqualTo(testKey)
        val stringJPNoLocaleDefault = localizationService.getStringOrDefault(testKey, "pouet")
        assertThat(stringJPNoLocaleDefault).isEqualTo("pouet")
    }

    @Test
    fun whenGettingString_differentLocale_shouldRetrieveDifferentLocale() {
        val testValueJP = "こんにちは"
        translationRepository.loadJsonMap(
            mapOf(
                Locale.GERMANY to jsonTextDE,
                Locale.JAPAN to jsonTextJP
            )
        )
        assertThat(translationRepository.getSupportedLocales()).containsExactly(Locale.GERMANY, Locale.JAPAN)
        App.setForcedLocale(Locale.JAPAN)
        // Different language with translation
        assertThat(localizationService.getString(testKey)).isEqualTo(testValueJP)
        App.setForcedLocale(Locale.GERMANY)
        assertThat(localizationService.getString(testKey)).isEqualTo(testValue)
    }

    @Test
    fun whenGettingDefaultLocaleString_getDefaultLocaleString() {
        val testValueJP = "こんにちは"
        translationRepository.setFallbackLocale("ja-JP")
        translationRepository.loadJsonMap(
            mapOf(
                Locale.GERMANY to jsonTextDE,
                Locale.JAPAN to jsonTextJP
            )
        )
        assertThat(translationRepository.getSupportedLocales()).containsExactly(Locale.GERMANY, Locale.JAPAN)
        assertThat(translationRepository.fallbackLocale).isEqualTo(Locale.JAPAN)
        App.setForcedLocale(Locale.JAPAN)
        // Different language with translation
        assertThat(localizationService.getString(testKey)).isEqualTo(testValueJP)
        App.setForcedLocale(Locale.GERMANY)
        assertThat(localizationService.getString(testKey)).isEqualTo(testValue)
        assertThat(localizationService.getDefaultLocaleString(testKey)).isEqualTo(testValueJP)
        assertThat(localizationService.getDefaultLocaleString("wrongKey")).isEqualTo("wrongKey")
    }

    @Test
    fun whenGettingDefaultLocaleString_withSomethingWrong_shouldGetKey() {
        translationRepository.setFallbackLocale("ja-JP")
        assertThat(translationRepository.fallbackLocale).isNotNull
        assertThat(localizationService.getDefaultLocaleString("wrongKey")).isEqualTo("wrongKey")
    }

    @Test
    fun getQuantityString() {
        App.setForcedLocale(Locale.GERMANY)
        val quantityKey = "hello_friend"
        translationRepository.loadJsonMap(mapOf(App.locale to jsonTextDE))
        assertThat(localizationService.getQuantityString(quantityKey, 1)).isEqualTo("Hello friend")
        assertThat(
            localizationService.getQuantityString(
                quantityKey,
                18
            )
        ).isEqualTo("Hello friends")
        assertThat(
            localizationService.getQuantityStringOrDefault(
                quantityKey,
                18,
                "pouet"
            )
        ).isEqualTo("Hello friends")

    }

    @Test
    fun getQuantityString_WithOnlyFallback_shouldReturnSomething() {
        App.setForcedLocale(null)
        translationRepository.setFallbackLocale("de-DE")
        val quantityKey = "hello_friend"
        assertThat(
            localizationService.getQuantityString(
                quantityKey,
                18
            )
        ).isEqualTo("hello_friend")

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        App.setForcedLocale(Locale.JAPAN)

        assertThat(
            localizationService.getQuantityString(
                quantityKey,
                18
            )
        ).isEqualTo("Hello friends")
    }

    @Test
    fun getQuantityStringForJapan_withGermanString_withFrenchFallback_shouldDefault() {
        val quantityKey = "hello_friend"

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        translationRepository.setFallbackLocale("fr-FR")

        App.setForcedLocale(Locale.JAPAN)
        assertThat(localizationService.getQuantityString(quantityKey, 1)).isEqualTo(quantityKey)
        assertThat(localizationService.getQuantityString(quantityKey, 18)).isEqualTo(quantityKey)
        assertThat(
            localizationService.getQuantityStringOrDefault(
                quantityKey,
                18,
                "pouet"
            )
        ).isEqualTo("pouet")
        assertThat(
            localizationService.getQuantityStringOrDefault(
                "doot",
                18,
                "pouet"
            )
        ).isEqualTo("pouet")
    }

    @Test
    fun getQuantityStringSwitzerland() {
        val quantityKey = "hello_friend"

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))

        App.setForcedLocale("de-CH".toLocale())
        assertThat(localizationService.getQuantityString(quantityKey, 1)).isEqualTo("Hello friend")
        assertThat(
            localizationService.getQuantityString(
                quantityKey,
                18
            )
        ).isEqualTo("Hello friends")
    }

    @Test
    fun getQuantityStringDefault() {
        App.setForcedLocale(Locale.GERMANY)
        val quantityKey = "hello_friend"

        translationRepository.loadJsonMap(mapOf(Locale.GERMANY to jsonTextDE))
        translationRepository.setFallbackLocale("de-DE")

        assertThat(localizationService.getQuantityString(quantityKey, 1)).isEqualTo("Hello friend")
        assertThat(
            localizationService.getQuantityString(
                quantityKey,
                18
            )
        ).isEqualTo("Hello friends")
    }
}
