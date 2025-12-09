package com.greencopper.event.scheduleItem

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.event.scheduleItem.viewmodel.Search
import com.greencopper.event.scheduleItem.viewmodel.SelectedView
import com.greencopper.event.scheduleItem.viewmodel.TimelineData
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.filteringbar.FilteringButton
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ScheduleInitializerTest {

    private val initializer = ScheduleInitializer()

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
        val widgetInfo: WidgetCollectionConfiguration.Instance.WidgetInfo = KiboSerializable.decodeFromString("""
                            {
                              "key": {
                                "name": "InterfaceKit.Widget.Image",
                                "version": 1
                              },
                              "params": {
                                "imageName": "imagewidget_image_20210323055120_185a99cc.png",
                                "analytics": {
                                  "itemName": "EventPass"
                                }
                              }
                            }
                    """.trimIndent()
        )
        val selected = FilteringButton.Button(
            title = "selectedButton",
            accessibilityLabel = "accessibility"
        )
        val unselected = selected.copy(title = "unselectedButton")
        val parameters = ScheduleData(
            emptyScheduleImage = "empty",
            onScheduleItemTap = "",
            filtering = null,
            search = Search("routeLink"),
            analytics = ScreenNameAnalytics("TestScreen"),
            defaultUI = SelectedView.List,
            timeline = TimelineData(
                displayToggle = false,
                buttonIcon = "icon",
                emptyStateImage = "empty_state"
            ),
            collections = listOf(
                WidgetCollectionData(
                    3,
                    WidgetCollectionConfiguration.Instance(null, listOf(widgetInfo))
                )
            ),
            favoritesEditing = FavoritesEditing(
                FavoritesEditing.Icon("addIcon", "add_label"),
                FavoritesEditing.Icon("removeIcon", "remove_label")
            ),
            myFavorites = FavoriteConfig(
                false,
                FilteringButton(selected, unselected),
                emptyPage = EmptyPage("title", "subtitle", "image")
            )
        )
        val layout = initializer.getLayout(parameters.encodeToJsonElement())
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isNotNull
    }
}
