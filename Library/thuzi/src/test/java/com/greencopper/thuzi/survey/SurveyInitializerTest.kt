package com.greencopper.thuzi.survey

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SurveyInitializerTest {

    private val localization = MockLocalizationService()
    private val localStorage = LocalStorage("project")
    private val initializer: SurveyInitializer
        get() = SurveyInitializer(localization, localStorage)

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    private val params: FeatureParams
        get() = KiboSerializable.decodeFromString(
            """
        {
            "url": "url_to_localize",
            "analytics": {
                "screenName": "analytics_screen_name",
                "itemId" : "analytics_item_id"
            }
        }
    """.trimIndent()
        )

    @Test
    fun whenGettingLayout_withoutParams_shouldGetLayout() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        mockBundleConstructor()
        val layout = initializer.getLayout(params)
        Assertions.assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefaultHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        Assertions.assertThat(redirectionHash).isEqualTo(RedirectionHash(SurveyInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefaultHash() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        Assertions.assertThat(redirectionHash).isEqualTo(RedirectionHash(SurveyInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(params)
        Assertions.assertThat(redirectionHash).isNotNull
    }
}
