package com.greencopper.interfacekit.sample

import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.sample.ui.SampleFragment
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SampleFeatureInitializerTest {

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        mockBundleConstructor()
    }

    private val data = SampleData("Sample Text", "Image Name")
    private val featureParams: FeatureParams = data.encodeToJsonElement()

    private val initializer = SampleFeatureInitializer()

    @Test
    fun correctParams_layoutForParams_returnsLayout() {
        val layout = initializer.getLayout(featureParams)
        assertThat(layout).isInstanceOf(SampleFragment::class.java)
    }

    @Test
    fun correctParams_redirectionHashForParams_returnsFullHash() {
        val hash = initializer.redirectionHashFor(featureParams)
        assertThat(hash.identifier).isNotNull
    }
}
