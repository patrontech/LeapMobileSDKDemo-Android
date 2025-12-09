package com.greencopper.core.localization.service

import android.content.Context
import com.greencopper.core.R
import com.greencopper.core.localization.translation.TranslationRepository
import com.greencopper.toolkit.locale.toList
import java.util.*

internal class JsonLocalizationService(
    private val context: Context,
    private val translationRepository: TranslationRepository
) : LocalizationService {

    override fun getStringFromRepository(key: String): String? {
        return getContextLocales().firstNotNullOfOrNull { locale ->
            translationRepository.getString(key, locale)
                ?: translationRepository.getSupportedLocaleForLanguage(locale)?.let {
                    translationRepository.getString(key, it)
                }
        } ?: translationRepository.getString(key, translationRepository.fallbackLocale)
    }

    override fun getDefaultLocaleString(key: String): String =
        translationRepository.getString(key, translationRepository.fallbackLocale) ?: key

    override fun getQuantityStringFromRepository(key: String, quantity: Int): String? {
        val pluralKey = context.resources.getQuantityString(R.plurals.plural_key_resolver, quantity)

        return getContextLocales().firstNotNullOfOrNull { locale ->
            translationRepository.getQuantityString(key, pluralKey, locale)
                ?: translationRepository.getSupportedLocaleForLanguage(locale)?.let {
                    translationRepository.getQuantityString(key, pluralKey, it)
                }
        } ?: translationRepository.getQuantityString(
            key,
            pluralKey,
            translationRepository.fallbackLocale
        )
    }

    private fun getContextLocales(): List<Locale> =
        context.resources.configuration.locales.toList()
}