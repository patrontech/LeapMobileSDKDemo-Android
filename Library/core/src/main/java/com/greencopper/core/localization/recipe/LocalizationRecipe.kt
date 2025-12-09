package com.greencopper.core.localization.recipe

import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.TryContentRecipe
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import com.greencopper.core.localization.translation.TranslationRepository
import com.greencopper.toolkit.App
import com.greencopper.toolkit.locale.toLocale
import kotlinx.serialization.json.Json
import java.io.File

internal open class LocalizationRecipe(
    private val jsonParser: Json,
    private val localStorageTranslationRepository: TranslationRepository
) : TryContentRecipe {
    override val key: ContentRecipeKey = ContentRecipeKey("Core.Strings", 1, 2)
    override val componentPath: String = "core/strings/"

    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {
        require(unarchivedDirectory.isDirectory) { "Unarchived directory is not a proper directory" }
        require(unarchivedDirectory.config().exists()) { "No config file found" }
        val localizationConfig = KiboSerializable.decodeFromString<LocalizationConfiguration>(
            unarchivedDirectory.config().readText()
        )
        require(localizationConfig.locales.contains(localizationConfig.defaultLocale))
        localizationConfig.locales.filterNot { it.isBlank() }.forEach { locale ->
            val localeFile = File(unarchivedDirectory, "$locale.json")
            require(localeFile.exists()) { "Locale file ${localeFile.name} not found" }
            require(localeFile.isValidJson()) { "Locale file ${localeFile.name} isn't valid JSON" }
            File(contentDirectory, "$locale.json").writeText(localeFile.readText())
        }
        // Write down the new config.json to content path
        try {
            localizationConfig.writeToPath(contentDirectory.config())
        } catch (exception: Exception) {
            throw exception
        }
    }

    override suspend fun tryToApply(contentDirectory: File) {
        val localizationConfig = KiboSerializable.decodeFromString<LocalizationConfiguration>(
            contentDirectory.config().readText()
        )

        App.setConfigLocale(localizationConfig.locales.map { it.toLocale() }, localizationConfig.defaultLocale.toLocale())

        localStorageTranslationRepository.setFallbackLocale(localizationConfig.defaultLocale)
        localStorageTranslationRepository.loadTranslations(localizationConfig, contentDirectory)
    }

    private fun File.isValidJson(): Boolean {
        return try {
            jsonParser.parseToJsonElement(this.readText())
            true
        } catch (e: Exception) {
            false
        }
    }
}
