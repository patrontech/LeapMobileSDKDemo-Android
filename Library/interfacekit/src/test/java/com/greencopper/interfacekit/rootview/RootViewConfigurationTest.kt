package com.greencopper.interfacekit.rootview

import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RootViewConfigurationTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun accessingAttributes() {
        val key = FeatureKey("InterfaceKit.TabBar", 1)
        val featureInfo = FeatureInfo(key, null)
        val rootViewConfiguration = RootViewConfiguration(featureInfo)
        assertThat(rootViewConfiguration.feature.key.name).isEqualTo("InterfaceKit.TabBar")
        assertThat(rootViewConfiguration.feature.key.version).isEqualTo(1)
        assertThat(rootViewConfiguration.feature.params).isNull()
    }

    @Test
    fun serialization() {
        val key = FeatureKey("InterfaceKit.TabBar", 1)
        val featureInfo = FeatureInfo(key, null)
        val rootViewConfiguration = RootViewConfiguration(featureInfo)
        val configString = json.encodeToString(RootViewConfiguration.serializer(), rootViewConfiguration)
        val serializedConfiguration = json.decodeFromString(RootViewConfiguration.serializer(), configString)
        assertThat(serializedConfiguration).isEqualTo(rootViewConfiguration)
    }
}
