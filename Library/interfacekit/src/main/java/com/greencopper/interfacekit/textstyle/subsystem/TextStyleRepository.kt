package com.greencopper.interfacekit.textstyle.subsystem

import android.graphics.Typeface
import com.greencopper.core.asset.manager.AssetsStorageManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.w
import kotlinx.serialization.json.*

public interface TextStyleRepository {
    public fun loadTextStyles(configuration: TextStyleConfiguration)
    public fun getIKFont(
        levels: List<String>,
        textStyle: IKFont.TextStyle = IKFont.TextStyle.bodyS,
        vararg fallbacks: IKFont,
    ): IKFont
}

internal class ConcreteTextStyleRepository(
    private val assetsStorageManager: AssetsStorageManager,
) : TextStyleRepository {

    protected var theme: TextStyleConfiguration.Theme = themeBackup

    @Suppress("RedundantGetter")
    private val cachedTypefaces: MutableMap<String, Typeface> = mutableMapOf()
        get() = field //Redundant getter to make it accessible in tests

    override fun loadTextStyles(configuration: TextStyleConfiguration) {
        theme = configuration.themes.getOrElse("defaultTheme") {
            throw IllegalStateException("No default theme was found in the text style config file.")
        }
    }

    override fun getIKFont(levels: List<String>, textStyle: IKFont.TextStyle, vararg fallbacks: IKFont): IKFont {
        val overrides = fallbacks.toList().flatMap { it.overrides }.toMutableList()

        getJsonData(levels)?.let {
            extractOverrideTextStyle(it)
        }?.let {
            overrides.add(0, it)
        }

        val fonts = (overrides + textStyle.getDefaultFromTheme(theme.default)).flatMap { it.fonts }

        val typeface = resolveTypeface(fonts, textStyle)

        return IKFont(textStyle, overrides, typeface)
    }

    private fun getJsonData(levels: List<String>): JsonObject? {
        var textStyleLevel: JsonObject = theme.override ?: return null
        levels.forEach {
            val levelValue = textStyleLevel[it] ?: return null
            textStyleLevel = levelValue.jsonObject
        }
        return textStyleLevel
    }

    private fun extractOverrideTextStyle(levelValue: JsonElement): TextStyleConfiguration.TextStyle? =
        try {
            // Use default Json to NOT ignore unknown keys
            Json.decodeFromJsonElement(TextStyleConfiguration.TextStyle.serializer(), levelValue)
        } catch (t: Throwable) {
            App.log.w("Couldn't extract textStyle from JsonElement $levelValue")
            null
        }

    private val supportedFonts: List<String> =
        assetsStorageManager.getAssetManager()
            .list("${assetsStorageManager.getRelativeAssetsDirectoryPath()}/$assetsFontDirectoryPath")?.toList()
            ?.let { assets ->
                assets.filter { assetName ->
                    assetName.endsWith(".otf") || (assetName.endsWith(".ttf"))
                }
            } ?: emptyList()

    private fun resolveTypeface(
        fonts: List<TextStyleConfiguration.TextStyle.Font>,
        textStyle: IKFont.TextStyle,
    ): Typeface {
        val matchingFonts = fonts.mapNotNull { font ->
            supportedFonts.find {
                it == "${font.name}.otf"
                        || it == "${font.name}.ttf"
            }
        }

        return matchingFonts.firstNotNullOfOrNull { assetName ->
            try {
                val assetPath =
                    "${assetsStorageManager.getRelativeAssetsDirectoryPath()}/$assetsFontDirectoryPath/$assetName"
                cachedTypefaces[assetName] ?: Typeface.createFromAsset(
                    assetsStorageManager.getAssetManager(),
                    assetPath
                )
                    ?.also {
                        cachedTypefaces[assetName] = it
                    }
            } catch (throwable: Throwable) {
                null
            }
        } ?: cachedTypefaces[textStyle.name] ?: textStyle.fallbackFont.also {
            cachedTypefaces[textStyle.name] = it
        }
    }

    private companion object {
        const val assetsFontDirectoryPath: String = "fonts"

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
    }
}
