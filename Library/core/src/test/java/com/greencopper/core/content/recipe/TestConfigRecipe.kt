package com.greencopper.core.content.recipe

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class TestConfig(val content: String): KiboSerializable<TestConfig> {
    override fun getSerializer(): KSerializer<TestConfig> = serializer()
}

internal class TestConfigHolder: ConfigurationHolder<TestConfig>()

internal class TestConfigRecipe(configHolder: TestConfigHolder): ConfigurationHolderRecipe<TestConfig, TestConfigHolder>(
    configHolder,
    KiboSerializable.Companion::decodeFromString,
    TestConfig::writeToPath
) {
    override val key: ContentRecipeKey = ContentRecipeKey("Test", 1, 1)
    override val componentPath: String = "test"
}