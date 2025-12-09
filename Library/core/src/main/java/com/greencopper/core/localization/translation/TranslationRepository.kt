package com.greencopper.core.localization.translation

import com.greencopper.core.localization.recipe.LocalizationConfiguration
import java.io.File
import java.util.*

internal typealias StringKey = String
internal typealias Quantity = String
internal typealias TranslationMap = MutableMap<Locale, Map<StringKey, String>>
internal typealias PluralsMap = MutableMap<Locale, Map<StringKey, Map<Quantity, String>>>

public interface TranslationRepository {

    public val fallbackLocale: Locale

    public fun loadTranslations(configuration: LocalizationConfiguration, contentDir: File)
    public fun loadJsonMap(translationsJson: Map<Locale, String>)
    public fun getString(key: String?, locale: Locale): String?
    public fun getQuantityString(stringKey: String?, pluralKey: String, locale: Locale): String?
    public fun getSupportedLocales(): Set<Locale>
    public fun setFallbackLocale(locale: String)

    public fun getSupportedLocaleForLanguage(locale: Locale): Locale? =
        getSupportedLocales().firstOrNull { locale.language == it.language }
}
