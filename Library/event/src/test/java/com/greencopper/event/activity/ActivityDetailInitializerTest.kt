package com.greencopper.event.activity

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ActivityDetailInitializerTest {

    private val initializer = ActivityDetailInitializer()

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
        val params = ActivityDetailData(
            activityId = 24,
            MyScheduleEditingInfo(
                MyScheduleEditingInfo.ButtonDetail("addIcon", ""),
                MyScheduleEditingInfo.ButtonDetail("removeIcon", ""),
                true,
            ),
            "icon",
            null,
            listOf(DisplayableTag("tag")),
            "onScheduleItemTap",
            false,
            ScreenNameAnalytics("TestScreen"),
        ).encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(ActivityDetailInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val params = App.resolve<Json>().encodeToJsonElement(
            ActivityDetailData.serializer(), ActivityDetailData(
                activityId = 24,
                MyScheduleEditingInfo(
                    MyScheduleEditingInfo.ButtonDetail("addIcon", ""),
                    MyScheduleEditingInfo.ButtonDetail("removeIcon", ""),
                    true,
                ),
                "icon",
                null,
                listOf(DisplayableTag("tag")),
                null,
                false,
                ScreenNameAnalytics("TestScreen"),
            )
        )
        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }
}
