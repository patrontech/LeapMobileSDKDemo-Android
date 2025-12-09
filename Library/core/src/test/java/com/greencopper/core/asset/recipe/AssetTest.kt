package com.greencopper.core.asset.recipe

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AssetTest {

    private val json: Json

    init {
        Toolkit.setupTest()
        json = App.resolve()
    }

    @Test
    fun serialize_deserialize() {
        val asset = Asset(
            name = "image.png",
            url = "https://image.url",
            onDemandOnly = true,
            formats = mapOf(
                "formatName" to Asset.Format(Asset.Format.Point(1, 2), Asset.Format.Size(1, 2))
            ),
            ratio = 1f,
            priority = 200
        )

        val assetBis = json.decodeFromString<Asset>(json.encodeToString(asset))

        assertThat(asset.name).isEqualTo(assetBis.name)
        assertThat(asset.formats?.getOrDefault("formatName", "1"))
            .isEqualTo(assetBis.formats?.getOrDefault("formatName", "2"))
    }

    @Test
    fun deserialize_withMissingOptionalParameters() {
        val assetString = """
            {
                "name": "image.png",
                "url": "https://image.url"
            }
        """.trimIndent()

        val assetBis = json.decodeFromString<Asset>(assetString)

        assertThat(assetBis.name).isEqualTo("image.png")
    }

}
