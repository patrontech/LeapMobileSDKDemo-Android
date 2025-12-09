package com.greencopper.event.performers

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PerformerDetailInitializerTest {
    private val initializer = PerformerDetailInitializer()

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(buildJsonObject { put("testKey", "testValue") })
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        mockBundleConstructor()
        val params = PerformerDetailData(
            performerId = "24",
            stageDetailIcon = "icon",
            analytics = ScreenNameAnalytics("TestScreen"),
        ).encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(PerformerDetailInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val params = App.resolve<Json>().encodeToJsonElement(
            PerformerDetailData.serializer(), PerformerDetailData(
                performerId = "24",
                stageDetailIcon = "icon",
                analytics = ScreenNameAnalytics("TestScreen"),
            )
        )
        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }

    @Test
    fun testSerializable_data() {
        testKiboSerializable(
            PerformerDetailData(
                performerId = "11",
                stageDetailIcon = "icon",
                myScheduleEditing = null,
                onScheduleItemTap = null,
                favoritesEditing = null,
                analytics = ScreenNameAnalytics(
                    screenName = "screenName",
                )
            )
        )
    }

}
