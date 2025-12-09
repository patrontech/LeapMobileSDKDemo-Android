package com.greencopper.event.scheduleItem

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.event.scheduleItem.ui.scheduledetail.ScheduleItemDetailData
import com.greencopper.event.scheduleItem.ui.scheduledetail.ScheduleItemDetailInitializer
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ScheduleItemDetailInitializerTest {

    private val initializer = ScheduleItemDetailInitializer()

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        org.junit.jupiter.api.assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        org.junit.jupiter.api.assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(buildJsonObject { put("testKey", "testValue") })
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        mockBundleConstructor()
        val params = ScheduleItemDetailData(
            scheduleItemId = 24,
            MyScheduleEditingInfo(
                MyScheduleEditingInfo.ButtonDetail("addIcon", ""),
                MyScheduleEditingInfo.ButtonDetail("removeIcon", ""),
                true,
            ),
            listOf(DisplayableTag("tag")),
            "icon",
            false,
            ScreenNameAnalytics("TestScreen")
        ).encodeToJsonElement()
        val layout = initializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(ScheduleItemDetailInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(buildJsonObject { put("testKey", "testValue") })
        assertThat(redirectionHash).isEqualTo(RedirectionHash(ScheduleItemDetailInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val params = ScheduleItemDetailData(
            scheduleItemId = 24,
            MyScheduleEditingInfo(
                MyScheduleEditingInfo.ButtonDetail("addIcon", ""),
                MyScheduleEditingInfo.ButtonDetail("removeIcon", ""),
                true,
            ),
            listOf(DisplayableTag("tag")),
            "icon",
            false,
            ScreenNameAnalytics("TestScreen")
        ).encodeToJsonElement()
        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }
}
