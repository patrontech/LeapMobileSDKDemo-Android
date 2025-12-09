package com.greencopper.core.localization

import com.greencopper.core.localization.translation.LocalStorageTranslationRepository
import com.greencopper.core.localization.translation.TranslationRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class LocalStorageTranslationRepositoryTest {

    init {
        Toolkit.setupTest()
    }

    private val translationRepository = LocalStorageTranslationRepository(App.resolve())

    @Test
    fun whenGettingString_multipleLocales_getDifferentStrings() {
        val key = "testKey"
        val value = "testValue"

        val jsonCA = """{
                      "$key": "${value}CA"
                    }"""
        translationRepository.loadJsonMap(mapOf(Locale.CANADA to jsonCA))

        val stringCA = translationRepository.getString("testKey", Locale.CANADA)
        assertThat(stringCA).isEqualTo("${value}CA")

        val stringJPNoLocale = translationRepository.getString("testKey", Locale.JAPAN)
        assertThat(stringJPNoLocale).isNull()

        val someOtherKey = "someOtherKey"
        val someOtherValue = "someOtherValue"
        val jsonJP = """{
                      "$someOtherKey": "${someOtherValue}JP"
                    }"""
        translationRepository.loadJsonMap(mapOf(Locale.JAPAN to jsonJP))
        val stringJPNoKeyValue = translationRepository.getString("testKey", Locale.JAPAN)
        assertThat(stringJPNoKeyValue).isNull()
    }

    @Test
    fun whenGettingQuantityString_withoutFallback_shouldBeNull() {
        val key = "testKey"

        // No fallback
        val emptyStringCA =
            translationRepository.getQuantityString(key, "other", Locale.CANADA)
        assertThat(emptyStringCA).isNull()
    }

    @Test
    fun whenGettingQuantityString_withValues_shouldBeCorrectString() {
        val key = "testKey"
        val otherValue = "testValueOther"
        val oneValue = "testValueOne"
        val someOtherKey = "someOtherKey"
        val someOtherOneValue = "someOthervalueOne"

        val jsonCA = """{
                      "$key":  {
                        "one": "${oneValue}CA",
                        "other": "${otherValue}CA"
                      },
                      "$someOtherKey":  {
                        "one": "${someOtherOneValue}CA"
                      }
                    }"""

        translationRepository.loadJsonMap(mapOf(Locale.CANADA to jsonCA))

        // Normal behaviour
        val stringCA = translationRepository.getQuantityString("testKey", "other", Locale.CANADA)
        assertThat(stringCA).isEqualTo("${otherValue}CA")
    }

    @Test
    fun whenGettingQuantityString_unsetLocale_shouldBeNull() {
        val key = "testKey"

        // No locale
        val stringJPNoLocale =
            translationRepository.getQuantityString(key, "other", Locale.JAPAN)
        assertThat(stringJPNoLocale).isNull()
    }

    @Test
    fun whenGettingQuantityString_withoutStringKey_shouldBeNull() {
        val key = "testKey"
        val someOtherKey = "someOtherKey"
        val someOtherValue = "someOtherValue"

        val jsonJP = """{
                      "$someOtherKey":  {
                        "other": "${someOtherValue}JP"
                      }
                    }"""

        translationRepository.loadJsonMap(mapOf(Locale.JAPAN to jsonJP))

        // No stringKey
        val stringJPNoKeyValue =
            translationRepository.getQuantityString(key, "other", Locale.JAPAN)
        assertThat(stringJPNoKeyValue).isNull()
    }

    @Test
    fun whenGettingQuantityString_withoutPluralKey_shouldBeNull() {
        val someOtherKey = "someOtherKey"
        val someOtherValue = "someOtherValue"

        val jsonJP = """{
                      "$someOtherKey":  {
                        "other": "${someOtherValue}JP"
                      }
                    }"""

        translationRepository.loadJsonMap(mapOf(Locale.JAPAN to jsonJP))

        // No pluralKey
        val stringJPNoQuantityKeyValue =
            translationRepository.getQuantityString(someOtherKey, "one", Locale.JAPAN)
        assertThat(stringJPNoQuantityKeyValue).isNull()
    }

    @Test
    fun whenGettingQuantityString_withoutStringKeyWithFallback_shouldBeFallback() {
        val someOtherKey = "someOtherKey"
        val someOtherValue = "someOtherValue"

        val jsonCA = """{
                      "$someOtherKey":  {
                        "other": "${someOtherValue}CA"
                      }
                    }"""

        translationRepository.loadJsonMap(mapOf(Locale.CANADA to jsonCA))

        // No stringKey fallback
        val stringFallbackNoKeyValue =
            translationRepository.getQuantityString("nonExistentKey", "other", Locale.CANADA)
        assertThat(stringFallbackNoKeyValue).isNull()
    }

    @Test
    fun whenGettingQuantityString_withoutPluralKeyWithFallback_shouldBeFallback() {
        val someOtherKey = "someOtherKey"
        val someOtherValue = "someOtherValue"
        val translationRepository: TranslationRepository = LocalStorageTranslationRepository(App.resolve())

        val jsonJP = """{
                      "$someOtherKey":  {
                        "other": "${someOtherValue}JP"
                      }
                    }"""

        translationRepository.loadJsonMap(mapOf(Locale.JAPAN to jsonJP))

        // No pluralKey fallback
        val stringFallbackNoQuantityKeyValue =
            translationRepository.getQuantityString(
                someOtherKey,
                "nonExistentQuantity",
                Locale.JAPAN
            )
        assertThat(stringFallbackNoQuantityKeyValue).isNull()
    }
}