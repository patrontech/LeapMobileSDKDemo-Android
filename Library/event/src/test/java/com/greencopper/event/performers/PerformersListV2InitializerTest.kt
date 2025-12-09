package com.greencopper.event.performers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.color.DefaultColors
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.filtering.filteringbar.FilteringButton
import com.greencopper.interfacekit.list.initializer.ListData
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.topbar.TopBarButton
import com.greencopper.interfacekit.topbar.TopBarData
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PerformersListV2InitializerTest {

    private val initializer = PerformersListV2Initializer()
    private val layoutDataProvider = MockLayoutDataProvider()

    @BeforeEach
    fun setUp() {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(layoutDataProvider)
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

        val widgetInfo: WidgetCollectionConfiguration.Instance.WidgetInfo = KiboSerializable.decodeFromString(
            """
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

        val params = ListData(
            statusBarColor = DefaultColors.StatusBar.Style.LIGHT,
            topBar = TopBarData(
                title = "title",
                rightButtons = listOf(TopBarButton.TextButton(text = "textRight", onTap = null)),
                leftButtons = listOf(TopBarButton.TextButton(text = "textLeft", onTap = null))
            ),
            mode = ListMode.Grid(2),
            onItemTap = "onItemTapRouteLink",
            analytics = ScreenNameAnalytics(screenName = "screenName"),
            emptyPage = EmptyPage(image = "image", title = "title", subtitle = "subtitle"),
            filtering = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            ),
            collections = listOf(
                WidgetCollectionData(
                    3,
                    WidgetCollectionConfiguration.Instance(null, listOf(widgetInfo))
                )
            ),
            favoritesEditing = FavoritesEditing(
                FavoritesEditing.Icon("add", "add_favorite"),
                FavoritesEditing.Icon("remove", "remove_favorite"),
            ),
            myFavorites = FavoriteConfig(
                activeOnLoad = false,
                filteringButton = FilteringButton(
                    FilteringButton.Button(
                        title = "titleSelected",
                        accessibilityLabel = "titleSelected"
                    ),
                    FilteringButton.Button(
                        title = "titleUnselected",
                        accessibilityLabel = "titleUnselected"
                    )
                ),
                emptyPage = EmptyPage(image = "image", title = "title", subtitle = "subtitle"),
            )
        )

        val expectedParams = ListLayoutData(
            statusBarColor = DefaultColors.StatusBar(
                DefaultColors.StatusBar.Style.LIGHT,
                DefaultColors.StatusBar.Style.LIGHT
            ),
            topBar = TopBarData(
                title = "title",
                rightButtons = listOf(TopBarButton.TextButton(text = "textRight", onTap = null)),
                leftButtons = listOf(TopBarButton.TextButton(text = "textLeft", onTap = null))
            ),
            mode = ListMode.Grid(2),
            onItemTapRouteLink = "onItemTapRouteLink",
            routeLinkKeyId = "performerId",
            analytics = ListLayoutData.Analytics(
                screenName = "screenName",
                screenClass = "performers_list",
                addToMyFavoritesEventName = "my_performers/add",
                removeFromMyFavoritesEventName = "my_performers/remove",
            ),
            emptyPage = EmptyPage(image = "image", title = "title", subtitle = "subtitle"),
            filtering = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            ),
            widgetCollections = listOf(
                WidgetCollectionCellLayoutData(
                    3,
                    WidgetCollectionConfiguration.Instance(null, listOf(widgetInfo))
                )
            ),
            favoritesEditing = FavoritesEditing(
                FavoritesEditing.Icon("add", "add_favorite"),
                FavoritesEditing.Icon("remove", "remove_favorite"),
            ),
            myFavorites = FavoriteConfig(
                activeOnLoad = false,
                filteringButton = FilteringButton(
                    FilteringButton.Button(
                        title = "titleSelected",
                        accessibilityLabel = "titleSelected"
                    ),
                    FilteringButton.Button(
                        title = "titleUnselected",
                        accessibilityLabel = "titleUnselected"
                    )
                ),
                emptyPage = EmptyPage(image = "image", title = "title", subtitle = "subtitle"),
            ),
            providerKey = "ListProvider.Performers",
            favoritesManagerKey = "MyPerformers",
            redirectionHash = RedirectionHash(FeatureKey("Event.PerformersList", 2), params.analytics.screenName)
        )

        val layout = initializer.getLayout(params.encodeToJsonElement())
        layout.assertNotNull()
        val entries = layoutDataProvider.mapData.values
        assertThat(entries).hasSize(1)
        KiboSerializable.decodeFromString<ListLayoutData>(entries.first()) shouldBe expectedParams
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isNotNull
    }
}
