package com.greencopper.core.localization

import com.greencopper.core.localization.recipe.LocalizationConfiguration
import com.greencopper.core.localization.translation.TranslationRepository
import java.io.File
import java.util.*

internal class MockTranslationRepository(
    override val fallbackLocale: Locale = Locale.getDefault(),
    private val loadTranslationsResult: () -> Unit = {},
    private val loadJsonMapResult: () -> Unit = {},
    private val getStringResult: () -> String? = {""},
    private val getQuantityStringResult: () -> String? = {""},
    private val getLocalesResult: () -> Set<Locale> = { emptySet()},
    private val setFallbackLocaleResult: () -> Unit = {}
) : TranslationRepository {
    override fun loadTranslations(configuration: LocalizationConfiguration, contentDir: File) = loadTranslationsResult()

    override fun loadJsonMap(translationsJson: Map<Locale, String>) = loadJsonMapResult()

    override fun getString(key: String?, locale: Locale): String? = getStringResult()

    override fun getQuantityString(stringKey: String?, pluralKey: String, locale: Locale): String? = getQuantityStringResult()

    override fun getSupportedLocales(): Set<Locale> = getLocalesResult()

    override fun setFallbackLocale(locale: String) = setFallbackLocaleResult()
}