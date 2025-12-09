package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.MockFilteringPredicateComputed
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.viewmodel.IndexedWidgets
import com.greencopper.interfacekit.widgets.viewmodel.MockWidgetGenerator
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.interfacekit.MockListProvider
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.interfacekit.MockWidgetResolver
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.architecture.NoEffect
import com.toggl.komposable.test.testReduce
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ListReducerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private var listProvider = MockListProvider { emptyList() }
    private val favoritesManager = MockFavoritesManager<Long>()
    private val localizationService = MockLocalizationService()
    private val widgetResolver = MockWidgetResolver()
    private val conditionChecker = MockConditionChecker()
    private val logger = MockLogging()
    private val routeController = MockRouteController()
    private val interestsConfigurationHolder = InterestsConfigurationHolder()
    private val localStorage = App.resolve<LocalStorage>()

    private var initialState = ListState()
    private var initialData = createListData()

    private val reducer: ListReducer<Long> by lazy {
        spyk(
            ListReducer.create<Long>(
                listProvider = listProvider,
                favoritesManager = favoritesManager,
                localizationService = localizationService,
                listData = initialData,
                widgetResolver = widgetResolver,
                conditionChecker = conditionChecker,
                logger = logger,
                routeController = routeController,
                interestsConfigHolder = interestsConfigurationHolder,
                localStorage = localStorage,
                coroutineContext = dispatcher,
                json = App.resolve(),
            ),
            recordPrivateCalls = true,
        )
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Nested
    @DisplayName("ScreenLoaded Action")
    inner class ScreenLoaded {
        @Test
        fun `without layout setup, should setup layout + uiClient and load content`() = runTest {
            val layout = mockk<Layout>()
            val uiClient = ListReducer.UiClient { }

            reducer.testReduce(initialState, ListAction.ScreenLoaded(layout, uiClient)) { state, effect ->
                state shouldBe initialState.copy(
                    layout = layout,
                    content = ViewState.ContentState.Empty(
                        imageName = "emptyImage", title = "emptyTitle", subtitle = "emptySubtitle",
                        widgets = null,
                        screenName = "screenName"
                    )
                )
                uiClientFromReducer shouldBe uiClient

                effect shouldBeActions arrayOf(
                    ListAction.WidgetsChanged(emptyList()),
                    ListAction.User.InterestsUpdated(emptySet()),
                    ListAction.User.FavoritesIdsUpdated(emptySet())
                )
            }
        }

        @Test
        fun `with layout already setup, should just update layout and uiClient`() = runTest {
            val layout = mockk<Layout>()
            initialState = initialState.copy(layout = layout)
            val newLayout = mockk<Layout>()

            val uiClient = ListReducer.UiClient { }
            reducer.testReduce(initialState, ListAction.ScreenLoaded(newLayout, uiClient)) { state, effect ->
                state shouldBe initialState.copy(layout = newLayout)
                uiClientFromReducer shouldBe uiClient
                effect shouldBe NoEffect
            }
        }
    }

    @Nested
    @DisplayName("TappedMyFavorites Action")
    inner class TappedMyFavorites {
        @Test
        fun `action should update selectedList with favorites enabled`() = runTest {
            initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests))
            reducer.testReduce(initialState, ListAction.User.TappedMyFavorites(true)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests))
                effect shouldBe NoEffect
            }

            initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests, SelectedList.MyFavorites))
            reducer.testReduce(initialState, ListAction.User.TappedMyFavorites(true)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests))
                effect shouldBe NoEffect
            }
        }

        @Test
        fun `action should update selectedList with favorites disabled`() = runTest {
            initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests, SelectedList.MyFavorites))
            reducer.testReduce(initialState, ListAction.User.TappedMyFavorites(false)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyInterests))
                effect shouldBe NoEffect
            }

            initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests))
            reducer.testReduce(initialState, ListAction.User.TappedMyFavorites(false)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyInterests))
                effect shouldBe NoEffect
            }
        }
    }


    @Nested
    @DisplayName("TappedMyInterests Action")
    inner class TappedMyInterests {
        @Test
        fun `action should update selectedList with interests enabled`() = runTest {
            initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
            reducer.testReduce(initialState, ListAction.User.TappedMyInterests(true)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests))
                effect shouldBe NoEffect
            }

            initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests, SelectedList.MyFavorites))
            reducer.testReduce(initialState, ListAction.User.TappedMyInterests(true)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests))
                effect shouldBe NoEffect
            }
        }

        @Test
        fun `action should update selectedList with interests disabled`() = runTest {
            initialState = initialState.copy(selectedList = setOf(SelectedList.MyInterests, SelectedList.MyFavorites))
            reducer.testReduce(initialState, ListAction.User.TappedMyInterests(false)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
                effect shouldBe NoEffect
            }

            initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
            reducer.testReduce(initialState, ListAction.User.TappedMyInterests(false)) { state, effect ->
                state shouldBe initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
                effect shouldBe NoEffect
            }
        }
    }

    @Nested
    @DisplayName("TappedAddToMyFavorites Action")
    inner class TappedAddToMyFavorites {
        @Test
        fun `action should add item to FavoritesManager`() = runTest {
            reducer.testReduce(
                initialState,
                ListAction.User.TappedAddToMyFavorites(listItemId = 123L, "testName")
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                favoritesManager.favoriteIds shouldBe setOf(123L)
            }
        }

        @Test
        fun `with Id of different type, should do nothing`() = runTest {
            favoritesManager.favoriteIds.add(1L)
            reducer.testReduce(
                initialState,
                ListAction.User.TappedAddToMyFavorites(listItemId = "abc", "testName")
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                favoritesManager.favoriteIds shouldBe setOf(1L)
            }
        }
    }

    @Nested
    @DisplayName("TappedRemoveFromMyFavorites Action")
    inner class TappedRemoveFromMyFavorites {
        @Test
        fun `action should remove item to FavoritesManager`() = runTest {
            favoritesManager.favoriteIds.addAll(listOf(123L, 456L, 789L))
            reducer.testReduce(
                initialState,
                ListAction.User.TappedRemoveFromMyFavorites(listItemId = 123L, "testName")
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                favoritesManager.favoriteIds shouldBe setOf(456L, 789L)
            }
        }

        @Test
        fun `with Id of different type, should do nothing`() = runTest {
            favoritesManager.favoriteIds.addAll(listOf(123L, 456L, 789L))
            reducer.testReduce(
                initialState,
                ListAction.User.TappedRemoveFromMyFavorites(listItemId = "abc", "testName")
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                favoritesManager.favoriteIds shouldBe setOf(123L, 456L, 789L)
            }
        }
    }

    @Nested
    @DisplayName("TappedListItem Action")
    inner class TappedListItem {
        @Test
        fun `if layout is set, should resolve routeLink`() = runTest {
            initialState = initialState.copy(layout = mockk())

            reducer.testReduce(
                initialState,
                ListAction.User.TappedListItem(123L)
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                routeController.lastResolveRouteLink shouldBe initialData.onItemTapRouteLink
                routeController.lastResolveRouteLinkParams shouldBe mapOf(initialData.routeLinkKeyId to "123")
            }

            reducer.testReduce(
                initialState,
                ListAction.User.TappedListItem("123")
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                routeController.lastResolveRouteLink shouldBe initialData.onItemTapRouteLink
                routeController.lastResolveRouteLinkParams shouldBe mapOf(initialData.routeLinkKeyId to "\"123\"")
            }
        }

        @Test
        fun `if layout is not set, should do nothing`() = runTest {
            reducer.testReduce(
                initialState,
                ListAction.User.TappedListItem(123L)
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect

                routeController.lastResolveRouteLink shouldBe null
                routeController.lastResolveRouteLinkParams shouldBe null
            }
        }
    }

    @Nested
    @DisplayName("FavoritesIdsUpdated Action")
    inner class FavoritesIdsUpdated {
        @Test
        fun `if favoritesEditing not provided, should do nothing`() = runTest {
            reducer.testReduce(
                initialState,
                ListAction.User.FavoritesIdsUpdated(setOf(123L, 456L))
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect
            }
        }

        @Test
        fun `with favoritesEditing, showing Favorites, should reload everything`() = runTest {
            initialData = initialData.copy(
                favoritesEditing = FavoritesEditing(
                    add = FavoritesEditing.Icon(icon = "addIcon", accessibilityLabel = "addDescription"),
                    remove = FavoritesEditing.Icon(icon = "removeIcon", accessibilityLabel = "removeDescription")
                )
            )
            initialState = initialState.copy(
                selectedList = setOf(SelectedList.MyFavorites)
            )

            val expectedState = initialState.copy(
                content = ViewState.ContentState.Empty(
                    title = "mattis",
                    subtitle = "sem",
                    imageName = "Penny Olson",
                    widgets = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    screenName = "screenName"
                )
            )
            mockkStatic(::reloadItems)
            every { reloadItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedState

            reducer.testReduce(
                initialState,
                ListAction.User.FavoritesIdsUpdated(setOf(123L, 456L))
            ) { state, effect ->
                state shouldBe expectedState
                effect shouldBeAction ListAction.ItemsReloaded
            }
        }

        @Test
        fun `with favoritesEditing, showing full list, should reload items`() = runTest {
            initialData = initialData.copy(
                favoritesEditing = FavoritesEditing(
                    add = FavoritesEditing.Icon(icon = "addIcon", accessibilityLabel = "addDescription"),
                    remove = FavoritesEditing.Icon(icon = "removeIcon", accessibilityLabel = "removeDescription")
                )
            )
            val expectedState = initialState.copy(
                content = ViewState.ContentState.Empty(
                    title = "mattis",
                    subtitle = "sem",
                    imageName = "Penny Olson",
                    widgets = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    screenName = "screenName"
                )
            )

            mockkStatic(::reloadItems)
            every { reloadItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedState

            reducer.testReduce(
                initialState,
                ListAction.User.FavoritesIdsUpdated(setOf(123L, 456L))
            ) { state, effect ->
                state shouldBe expectedState
                effect shouldBeAction ListAction.ItemsReloaded
            }
        }
    }

    @Nested
    @DisplayName("FavoritesIdsUpdated Action")
    inner class InterestsUpdated {
        @Test
        fun `if myInterests data is not provided, should do nothing`() = runTest {
            reducer.testReduce(
                initialState,
                ListAction.User.InterestsUpdated(setOf("a", "b"))
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect
            }
        }

        @Test
        fun `with favoritesEditingmyInterestsData, showing MyInterests, should reload everything`() = runTest {
            initialData = initialData.copy(
                myInterests = IntegratedInterestsData(
                    activeOnLoad = false,
                    emptyPage = EmptyPage(
                        "myInterestsImage",
                        "myInterestsTitle",
                        "myInterestsSubtitle",
                        WidgetCollectionConfiguration.Instance(
                            widgets = listOf(
                                WidgetCollectionConfiguration.Instance.WidgetInfo(
                                    WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                    JsonArray(emptyList()),
                                )
                            )
                        )
                    )
                )
            )
            initialState = initialState.copy(
                selectedList = setOf(SelectedList.MyInterests)
            )

            val expectedState = initialState.copy(
                content = ViewState.ContentState.Empty(
                    title = "mattis",
                    subtitle = "sem",
                    imageName = "Penny Olson",
                    widgets = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    screenName = "screenName"
                )
            )
            mockkStatic(::reloadItems)
            every { reloadItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedState

            reducer.testReduce(
                initialState,
                ListAction.User.InterestsUpdated(setOf("a", "b"))
            ) { state, effect ->
                state shouldBe expectedState
                effect shouldBeAction ListAction.ItemsReloaded
            }
        }

        @Test
        fun `with myInterestsData, showing full list, should do nothing`() = runTest {
            initialData = initialData.copy(
                myInterests = IntegratedInterestsData(
                    activeOnLoad = false,
                    emptyPage = EmptyPage(
                        "myInterestsImage",
                        "myInterestsTitle",
                        "myInterestsSubtitle",
                        WidgetCollectionConfiguration.Instance(
                            widgets = listOf(
                                WidgetCollectionConfiguration.Instance.WidgetInfo(
                                    WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                    JsonArray(emptyList()),
                                )
                            )
                        )
                    )
                )
            )

            reducer.testReduce(
                initialState,
                ListAction.User.InterestsUpdated(setOf("a", "b"))
            ) { state, effect ->
                state shouldBe initialState
                effect shouldBe NoEffect
            }
        }
    }

    @Nested
    @DisplayName("FilteringUpdated Action")
    inner class FilteringUpdated {
        @Test
        fun `should reload items and resetScroll if UiClient provided`() = runTest {
            var uiClientCalled = false
            val uiClient = ListReducer.UiClient { uiClientCalled = true }
            val filteringPredicate = MockFilteringPredicateComputed("test123", false)

            uiClientFromReducer = uiClient

            val expectedState = initialState.copy(
                filteringPredicate = filteringPredicate,
                content = ViewState.ContentState.Empty(
                    title = "mattis",
                    subtitle = "sem",
                    imageName = "Penny Olson",
                    widgets = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    screenName = "screenName"
                )
            )
            mockkStatic(::reloadItems)
            every { reloadItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedState

            reducer.testReduce(
                initialState,
                ListAction.User.FilteringUpdated(filteringPredicate)
            ) { state, effect ->
                state shouldBe expectedState
                effect shouldBeAction ListAction.ItemsReloaded
                uiClientCalled shouldBe true
            }
        }

        @Test
        fun `should reload items and nothing if uiClient not provided`() = runTest {
            val filteringPredicate = MockFilteringPredicateComputed("test123", false)

            val expectedState = initialState.copy(
                filteringPredicate = filteringPredicate,
                content = ViewState.ContentState.Empty(
                    title = "mattis",
                    subtitle = "sem",
                    imageName = "Penny Olson",
                    widgets = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    screenName = "screenName"
                )
            )
            mockkStatic(::reloadItems)
            every { reloadItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedState

            reducer.testReduce(
                initialState,
                ListAction.User.FilteringUpdated(filteringPredicate)
            ) { state, effect ->
                state shouldBe expectedState
                effect shouldBeAction ListAction.ItemsReloaded
            }
        }
    }

    @Nested
    @DisplayName("WidgetsChanged Action")
    inner class WidgetsChanged {
        @Test
        fun `should update state and reload items`() = runTest {
            val indexedWidgets = listOf(
                IndexedWidgets(0, listOf(MockWidgetGenerator("1"))),
                IndexedWidgets(1, listOf(MockWidgetGenerator("3"))),
            )

            val expectedState = initialState.copy(
                authorizedWidgets = indexedWidgets,
                content = ViewState.ContentState.Empty(
                    title = "mattis",
                    subtitle = "sem",
                    imageName = "Penny Olson",
                    widgets = WidgetCollectionConfiguration.Instance(
                        widgets = listOf(
                            WidgetCollectionConfiguration.Instance.WidgetInfo(
                                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                JsonArray(emptyList()),
                            )
                        )
                    ),
                    screenName = "screenName"
                )
            )
            mockkStatic(::reloadItems)
            every { reloadItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns expectedState

            reducer.testReduce(
                initialState,
                ListAction.WidgetsChanged(indexedWidgets)
            ) { state, effect ->
                state shouldBe expectedState
                effect shouldBeAction ListAction.ItemsReloaded
            }
        }
    }

    @Test
    fun `unknown action should do nothing`() = runTest {
        reducer.testReduce(
            initialState,
            ListAction.ItemsReloaded
        ) { state, effect ->
            state shouldBe initialState
            effect shouldBe NoEffect
        }
    }

    private var uiClientFromReducer: ListReducer.UiClient
        get() = run {
            with(ListReducer::class.java.getDeclaredField("uiClient")) {
                isAccessible = true
                get(reducer) as ListReducer.UiClient
            }
        }
        set(uiClient) = run {
            with(ListReducer::class.java.getDeclaredField("uiClient")) {
                isAccessible = true
                set(reducer, uiClient)
            }
        }

}
