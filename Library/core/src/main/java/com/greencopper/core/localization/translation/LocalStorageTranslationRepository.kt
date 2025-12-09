package com.greencopper.core.localization.translation

import com.greencopper.core.localization.recipe.LocalizationConfiguration
import com.greencopper.toolkit.App
import com.greencopper.toolkit.locale.toLocale
import kotlinx.serialization.json.*
import java.io.File
import java.util.Locale

internal class LocalStorageTranslationRepository(private val json: Json) : TranslationRepository {

    override var fallbackLocale: Locale = App.locale

    private val translations: TranslationMap = mutableMapOf()

    private val plurals: PluralsMap = mutableMapOf()

    override fun loadTranslations(configuration: LocalizationConfiguration, contentDir: File) {
        translations.clear()
        plurals.clear()
        val translations = mutableMapOf<Locale, String>()
        configuration.locales.forEach { localeString ->
            val localeFile = File(contentDir, "$localeString.json")
            val locale = localeString.toLocale()
            translations[locale] = localeFile.readText()
        }
        loadJsonMap(translations)
    }

    override fun loadJsonMap(translationsJson: Map<Locale, String>) {
        translationsJson.forEach { (contentLocale, translationJson) ->
            val jsonMap = json.parseToJsonElement(translationJson).jsonObject.toMap()
            val translations =
                jsonMap.filter { it.value is JsonPrimitive }
                    .mapValues { it.value.jsonPrimitive.content }
            val plurals = jsonMap.filter { it.value is JsonObject }
                .mapValues {
                    it.value.jsonObject.toMap()
                        .mapValues { plurals -> plurals.value.jsonPrimitive.content }
                }

            this.translations[contentLocale] = translations
            this.plurals[contentLocale] = plurals
        }
    }

    override fun getString(key: String?, locale: Locale): String? {
        return translations[locale]?.get(key)
    }

    override fun getQuantityString(stringKey: String?, pluralKey: String, locale: Locale): String? {
        return plurals[locale]?.get(stringKey)?.get(pluralKey)
    }

    override fun getSupportedLocales(): MutableSet<Locale> {
        return translations.keys
    }

    override fun setFallbackLocale(locale: String) {
        fallbackLocale = locale.toLocale()
    }
}
