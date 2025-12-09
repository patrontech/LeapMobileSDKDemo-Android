package com.greencopper.maps.locationdetail

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class LocationDetailInitializerTest {

    private val initializer = LocationDetailInitializer()

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
    fun whenGettingLayout_withWrongParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(buildJsonObject { put("testKey", "testValue") })
        }
    }

    @Test
    fun whenGettingLayout_withCorrectParams_shouldGetLayout() {
        mockBundleConstructor()
        val favoritesEditing = FavoritesEditing(
            FavoritesEditing.Icon("add", "add_favorite"),
            FavoritesEditing.Icon("remove", "remove_favorite"),
        )
        val parameters = LocationDetailData("1", ScreenNameAnalytics("test"), favoritesEditing)
        val layout = initializer.getLayout(
            parameters.encodeToJsonElement()
        )
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isNotNull
    }
}
