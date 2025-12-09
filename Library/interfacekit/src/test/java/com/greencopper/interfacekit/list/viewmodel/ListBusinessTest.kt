package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.favorites.FavoriteConfig
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.interests.integration.IntegratedInterestsData
import com.greencopper.interfacekit.interests.recipe.Interest
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.interfacekit.widgets.viewmodel.IndexedWidgets
import com.greencopper.interfacekit.widgets.viewmodel.MockWidgetGenerator
import com.greencopper.testmocks.assertNotNull
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.shouldBe
import io.mockk.spyk
import io.mockk.verify
import kotlinx.serialization.json.JsonArray
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

internal class ListBusinessTest {

    private val localizationService = spyk(MockLocalizationService(
        getStringFromRepository = { it }
    ))

    private val lambdaRemoveAction: (Any, String) -> ListAction = { id, title ->
        ListAction.User.TappedRemoveFromMyFavorites(id, title)
    }
    private val lambdaAddAction: (Any, String) -> ListAction = { id, title ->
        ListAction.User.TappedAddToMyFavorites(id, title)
    }

    @Nested
    @DisplayName("reloadItems")
    inner class ReloadItems {

        var initialData = createListData()
        var initialState = ListState()

        @Test
        fun `empty unfiltered should show empty`() {
            testFunction(
                expectedContentState = ViewState.ContentState.Empty(
                    imageName = "emptyImage",
                    title = "emptyTitle",
                    subtitle = "emptySubtitle",
                    widgets = null,
                    screenName = "screenName",
                )
            )
        }

        @Test
        fun `filtering everything out should show empty`() {
            initialState = initialState.copy(
                filteringPredicate = FilteringPredicate.Tag("foo").query()
            )
            testFunction(
                unfilteredItems = generateElements(5), expectedContentState = ViewState.ContentState.Empty(
                    imageName = "emptyImage",
                    title = "emptyTitle",
                    subtitle = "emptySubtitle",
                    widgets = null,
                    screenName = "screenName"
                )
            )
        }

        @Test
        fun `filtering on specific tag should filter properly`() {
            initialState = initialState.copy(
                filteringPredicate = FilteringPredicate.Tag("tag2").query()
            )
            val unfilteredItems = generateElements(5)
            testFunction(
                unfilteredItems = unfilteredItems, expectedContentState = ViewState.ContentState.Content(
                    listOf(
                        unfilteredItems[2].toListViewDataItem(tapAction = lambdaAddAction),
                    )
                )
            )
        }

        @Test
        fun `filtering on specific tag with interests should filter properly`() {
            initialState = initialState.copy(
                filteringPredicate = FilteringPredicate.Logic(
                    FilteringPredicate.Tag("tag1"),
                    FilteringPredicate.Operator.OR,
                    FilteringPredicate.Tag("tag2"),
                ).query(),
                selectedList = setOf(SelectedList.MyInterests)
            )
            val unfilteredItems = generateElements(5)
            testFunction(
                unfilteredItems = unfilteredItems,
                configInterests = listOf(
                    Interest(
                        id = "2",
                        name = "name",
                        order = 0,
                        analyticsName = "analyticsName2",
                        tags = listOf("tag2")
                    ),
                    Interest(
                        id = "3",
                        name = "name",
                        order = 0,
                        analyticsName = "analyticsName3",
                        tags = listOf("tag3")
                    )
                ),
                myInterests = setOf("1", "2", "3"),
                expectedContentState = ViewState.ContentState.Content(
                    listOf(
                        unfilteredItems[2].toListViewDataItem(tapAction = lambdaAddAction),
                    )
                ),
            )
        }

        @Test
        fun `filtering with interestConfig but no interests should return empty`() {
            initialData = initialData.copy(
                myInterests = IntegratedInterestsData(
                    emptyPage = EmptyPage(
                        image = "interestsEmptyImage",
                        title = "interestsEmptyTitle",
                        subtitle = "interestsEmptySubtitle",
                    )
                )
            )
            initialState = initialState.copy(
                filteringPredicate = FilteringPredicate.Logic(
                    FilteringPredicate.Tag("tag1"),
                    FilteringPredicate.Operator.OR,
                    FilteringPredicate.Tag("tag2"),
                ).query(),
                selectedList = setOf(SelectedList.MyInterests)
            )
            val unfilteredItems = generateElements(5)
            testFunction(
                unfilteredItems = unfilteredItems,
                configInterests = listOf(
                    Interest(
                        id = "2",
                        name = "name",
                        order = 0,
                        analyticsName = "analyticsName2",
                        tags = listOf("tag2")
                    ),
                    Interest(
                        id = "3",
                        name = "name",
                        order = 0,
                        analyticsName = "analyticsName3",
                        tags = listOf("tag3")
                    )
                ),
                myInterests = emptySet(),
                expectedContentState = ViewState.ContentState.Empty(
                    title = "interestsEmptyTitle",
                    subtitle = "interestsEmptySubtitle",
                    imageName = "interestsEmptyImage",
                    widgets = null,
                    screenName = "screenName",
                ),
            )
        }

        @Test
        fun `load with different columns, without widgets, doesn't change result`() {
            val unfilteredItems = generateElements(5)
            val expectedContentState = ViewState.ContentState.Content(
                listOf(
                    unfilteredItems[0].toListViewDataItem(tapAction = lambdaAddAction),
                    unfilteredItems[1].toListViewDataItem(tapAction = lambdaAddAction),
                    unfilteredItems[2].toListViewDataItem(tapAction = lambdaAddAction),
                    unfilteredItems[3].toListViewDataItem(tapAction = lambdaAddAction),
                    unfilteredItems[4].toListViewDataItem(tapAction = lambdaAddAction),
                )
            )

            testFunction(
                unfilteredItems = unfilteredItems, expectedContentState = expectedContentState
            )

            initialData = initialData.copy(mode = ListMode.Grid(2))
            testFunction(
                unfilteredItems = unfilteredItems, expectedContentState = expectedContentState
            )
        }

        @Test
        fun `load item ordered differently, are sorted by order then name with null order last`() {
            val items = generateElements(
                ElementGeneratorItem("dest", 3),
                ElementGeneratorItem("nest"),
                ElementGeneratorItem("cest", 3),
                ElementGeneratorItem("aest"),
                ElementGeneratorItem("Aest"),
                ElementGeneratorItem("zest", 1),
                ElementGeneratorItem("best", 2),
            )

            testFunction(
                unfilteredItems = items, expectedContentState = ViewState.ContentState.Content(
                    listOf(
                        items[5].toListViewDataItem(tapAction = lambdaAddAction),
                        items[6].toListViewDataItem(tapAction = lambdaAddAction),
                        items[2].toListViewDataItem(tapAction = lambdaAddAction),
                        items[0].toListViewDataItem(tapAction = lambdaAddAction),
                        items[3].toListViewDataItem(tapAction = lambdaAddAction),
                        items[4].toListViewDataItem(tapAction = lambdaAddAction),
                        items[1].toListViewDataItem(tapAction = lambdaAddAction),
                    )
                )
            )
        }

        @Nested
        @DisplayName("withFavoritesConfig")
        inner class WithFavoritesConfig {
            private val favoritesEditing = FavoritesEditing(
                add = FavoritesEditing.Icon(icon = "addIcon", accessibilityLabel = "addDescription"),
                remove = FavoritesEditing.Icon(icon = "removeIcon", accessibilityLabel = "removeDescription")
            )

            init {
                initialData = initialData.copy(
                    myFavorites = FavoriteConfig(
                        emptyPage = EmptyPage(
                            image = "favoritesEmptyImage",
                            title = "favoritesEmptyTitle",
                            subtitle = "favoritesEmptySubtitle",
                        )
                    ),
                    favoritesEditing = favoritesEditing
                )
            }

            @Test
            fun `when empty, uses FavoriteConfig's EmptyPage`() {
                initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
                val items = generateElements(5)

                testFunction(
                    unfilteredItems = items,
                    favoritesIds = emptySet(),
                    expectedContentState = ViewState.ContentState.Empty(
                        imageName = "favoritesEmptyImage",
                        title = "favoritesEmptyTitle",
                        subtitle = "favoritesEmptySubtitle",
                        widgets = null,
                        screenName = "screenName"
                    )
                )
            }

            @Test
            fun `when empty, uses default EmptyPage if FavoriteConfig's EmptyPage is null`() {
                initialData = initialData.copy(
                    myFavorites = initialData.myFavorites?.copy(
                        emptyPage = EmptyPage(
                            image = "emptyImage",
                            title = "emptyTitle",
                            subtitle = "emptySubtitle",
                        )
                    )
                )
                initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
                val items = generateElements(5)

                testFunction(
                    unfilteredItems = items,
                    favoritesIds = emptySet(),
                    expectedContentState = ViewState.ContentState.Empty(
                        imageName = "emptyImage",
                        title = "emptyTitle",
                        subtitle = "emptySubtitle",
                        widgets = null,
                        screenName = "screenName"
                    )
                )
            }

            @Test
            fun `when favorites enabled, items are filtered out if not favorited`() {
                initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites))
                val items = generateElements(5)

                testFunction(
                    unfilteredItems = items,
                    favoritesIds = setOf(0L, 2L),
                    expectedContentState = ViewState.ContentState.Content(
                        listOf(
                            items[0].toListViewDataItem(
                                favIcon = favoritesEditing.remove,
                                tapAction = lambdaRemoveAction
                            ),
                            items[2].toListViewDataItem(
                                favIcon = favoritesEditing.remove,
                                tapAction = lambdaRemoveAction
                            ),
                        )
                    )
                )
            }

            @Test
            fun `when favorites and interests enabled, items are filtered out if not favorited nor in interests`() {
                initialState = initialState.copy(selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests))
                val items = generateElements(5)

                testFunction(
                    unfilteredItems = items,
                    favoritesIds = setOf(0L, 2L),
                    configInterests = listOf(
                        Interest(
                            id = "2",
                            name = "name",
                            order = 0,
                            analyticsName = "analyticsName2",
                            tags = listOf("tag2")
                        )
                    ),
                    myInterests = setOf("1", "2"),
                    expectedContentState = ViewState.ContentState.Content(
                        listOf(
                            items[2].toListViewDataItem(
                                favIcon = favoritesEditing.remove,
                                tapAction = lambdaRemoveAction
                            ),
                        )
                    )
                )
            }
        }

        @Nested
        @DisplayName("withWidgets")
        inner class WithWidgets {

            @Test
            fun `with 1 column, widgets should be placed by respecting columns`() {
                val items = generateElements(4)

                val widgetsMap = mapOf(
                    "01" to MockWidgetGenerator("01"),
                    "02" to MockWidgetGenerator("02"),
                    "11" to MockWidgetGenerator("11"),
                    "31" to MockWidgetGenerator("31"),
                )

                initialState = initialState.copy(
                    authorizedWidgets = listOf(
                        IndexedWidgets(0, listOf(widgetsMap["01"]!!, widgetsMap["02"]!!)),
                        IndexedWidgets(1, listOf(widgetsMap["11"]!!)),
                        IndexedWidgets(3, listOf(widgetsMap["31"]!!)),
                    )
                )

                testFunction(
                    unfilteredItems = items,
                    expectedContentState = ViewState.ContentState.Content(
                        listOf(
                            ListViewData.WidgetItem(0, widgetsMap["01"]!!),
                            ListViewData.WidgetItem(1, widgetsMap["02"]!!),
                            items[0].toListViewDataItem(tapAction = lambdaAddAction),
                            ListViewData.WidgetItem(3, widgetsMap["11"]!!),
                            items[1].toListViewDataItem(tapAction = lambdaAddAction),
                            items[2].toListViewDataItem(tapAction = lambdaAddAction),
                            ListViewData.WidgetItem(6, widgetsMap["31"]!!),
                            items[3].toListViewDataItem(tapAction = lambdaAddAction),
                        )
                    )
                )
            }

            @Test
            fun `with 3 column, widgets should be placed by respecting columns`() {
                initialData = initialData.copy(mode = ListMode.Grid(3))
                val items = generateElements(7)

                val widgetsMap = mapOf(
                    "01" to MockWidgetGenerator("01"),
                    "11" to MockWidgetGenerator("11"),
                    "21" to MockWidgetGenerator("21"),
                    "31" to MockWidgetGenerator("31"),
                    "61" to MockWidgetGenerator("61"),
                    "71" to MockWidgetGenerator("71"),
                )

                initialState = initialState.copy(
                    authorizedWidgets = listOf(
                        IndexedWidgets(0, listOf(widgetsMap["01"]!!)),
                        IndexedWidgets(1, listOf(widgetsMap["11"]!!)),
                        IndexedWidgets(2, listOf(widgetsMap["21"]!!)),
                        IndexedWidgets(3, listOf(widgetsMap["31"]!!)),
                        IndexedWidgets(6, listOf(widgetsMap["61"]!!)),
                        IndexedWidgets(7, listOf(widgetsMap["71"]!!)),
                    )
                )

                testFunction(
                    unfilteredItems = items,
                    expectedContentState = ViewState.ContentState.Content(
                        listOf(
                            ListViewData.WidgetItem(0, widgetsMap["01"]!!),
                            items[0].toListViewDataItem(tapAction = lambdaAddAction),
                            items[1].toListViewDataItem(tapAction = lambdaAddAction),
                            items[2].toListViewDataItem(tapAction = lambdaAddAction),
                            ListViewData.WidgetItem(4, widgetsMap["11"]!!),
                            items[3].toListViewDataItem(tapAction = lambdaAddAction),
                            items[4].toListViewDataItem(tapAction = lambdaAddAction),
                            items[5].toListViewDataItem(tapAction = lambdaAddAction),
                            ListViewData.WidgetItem(8, widgetsMap["21"]!!),
                            items[6].toListViewDataItem(tapAction = lambdaAddAction),
                            ListViewData.WidgetItem(10, widgetsMap["31"]!!),
                            ListViewData.WidgetItem(11, widgetsMap["61"]!!),
                            ListViewData.WidgetItem(12, widgetsMap["71"]!!),
                        )
                    )
                )
            }

            @Test
            fun `with no items, shouldn't show widgets`() {
                val widgetsMap = mapOf(
                    "01" to MockWidgetGenerator("01"),
                    "11" to MockWidgetGenerator("11"),
                )

                initialState = initialState.copy(
                    authorizedWidgets = listOf(
                        IndexedWidgets(0, listOf(widgetsMap["01"]!!)),
                        IndexedWidgets(1, listOf(widgetsMap["11"]!!)),
                    )
                )

                testFunction(
                    unfilteredItems = emptyList(),
                    expectedContentState = ViewState.ContentState.Empty(
                        imageName = "emptyImage",
                        title = "emptyTitle",
                        subtitle = "emptySubtitle",
                        widgets = null,
                        screenName = "screenName"
                    )
                )
            }

            @Test
            fun `with more widgets than items, should show the rest of widgets after`() {
                initialData = initialData.copy(mode = ListMode.Grid(3))
                val items = generateElements(3)

                val widgetsMap = mapOf(
                    "01" to MockWidgetGenerator("01"),
                    "11" to MockWidgetGenerator("11"),
                    "21" to MockWidgetGenerator("21"),
                    "31" to MockWidgetGenerator("31"),
                    "61" to MockWidgetGenerator("61"),
                    "71" to MockWidgetGenerator("71"),
                )

                initialState = initialState.copy(
                    authorizedWidgets = listOf(
                        IndexedWidgets(0, listOf(widgetsMap["01"]!!)),
                        IndexedWidgets(1, listOf(widgetsMap["11"]!!)),
                        IndexedWidgets(2, listOf(widgetsMap["21"]!!)),
                        IndexedWidgets(3, listOf(widgetsMap["31"]!!)),
                        IndexedWidgets(6, listOf(widgetsMap["61"]!!)),
                        IndexedWidgets(7, listOf(widgetsMap["71"]!!)),
                    )
                )

                testFunction(
                    unfilteredItems = items,
                    expectedContentState = ViewState.ContentState.Content(
                        listOf(
                            ListViewData.WidgetItem(0, widgetsMap["01"]!!),
                            items[0].toListViewDataItem(tapAction = lambdaAddAction),
                            items[1].toListViewDataItem(tapAction = lambdaAddAction),
                            items[2].toListViewDataItem(tapAction = lambdaAddAction),
                            ListViewData.WidgetItem(4, widgetsMap["11"]!!),
                            ListViewData.WidgetItem(5, widgetsMap["21"]!!),
                            ListViewData.WidgetItem(6, widgetsMap["31"]!!),
                            ListViewData.WidgetItem(7, widgetsMap["61"]!!),
                            ListViewData.WidgetItem(8, widgetsMap["71"]!!),
                        )
                    )
                )
            }
        }

        fun testFunction(
            unfilteredItems: List<ListProvider.Element> = emptyList(),
            favoritesIds: Set<Any> = emptySet(),
            configInterests: List<Interest> = emptyList(),
            myInterests: Set<String> = emptySet(),
            expectedContentState: ViewState.ContentState,
        ) {
            val resultState = reloadItems(
                state = initialState,
                listData = initialData,
                unfilteredItems = unfilteredItems,
                favoriteIds = favoritesIds,
                configInterests = configInterests,
                myInterests = myInterests,
                localizationService = localizationService,
                locale = Locale.getDefault(),
            )

            val expectedState = initialState.copy(content = expectedContentState)

            if (expectedState.content is ViewState.ContentState.Content) {
                val listItems = expectedState.content.items.filterIsInstance<ListViewData.ListItem>()
                verify(atLeast = listItems.size) {
                    localizationService.getString(any())
                }
            }

            resultState shouldBe expectedState
        }
    }

    @Nested
    inner class GetEmptyState {

        var initialData = createListData()

        @Test
        fun `with noSelectedList`() {
            testFunction(
                expectedResult = ViewState.ContentState.Empty(
                    imageName = "emptyImage",
                    title = "emptyTitle",
                    subtitle = "emptySubtitle",
                    widgets = null,
                    screenName = "screenName"
                )
            )
        }

        @Test
        fun `with myFavorites`() {
            initialData = initialData.copy(
                myFavorites = FavoriteConfig(
                    emptyPage = EmptyPage(
                        title = "favoritesEmptyTitle",
                        subtitle = "favoritesEmptySubtitle",
                        image = "favoritesEmpty",
                    )
                )
            )
            testFunction(
                selectedList = setOf(SelectedList.MyFavorites),
                expectedResult = ViewState.ContentState.Empty(
                    title = "favoritesEmptyTitle",
                    subtitle = "favoritesEmptySubtitle",
                    imageName = "favoritesEmpty",
                    widgets = null,
                    screenName = "screenName"
                )
            )
        }

        @Test
        fun `with myInterests`() {
            initialData = initialData.copy(
                myInterests = IntegratedInterestsData(
                    emptyPage = EmptyPage(
                        title = "interestsEmptyTitle",
                        subtitle = "interestsEmptySubtitle",
                        image = "interestsEmpty",
                        topWidgetCollection = WidgetCollectionConfiguration.Instance(
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
            testFunction(
                selectedList = setOf(SelectedList.MyInterests),
                expectedResult = ViewState.ContentState.Empty(
                    title = "interestsEmptyTitle",
                    subtitle = "interestsEmptySubtitle",
                    imageName = "interestsEmpty",
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
        }

        @Test
        fun `with myFavorites and myInterests with interests`() {
            initialData = initialData.copy(
                myInterests = IntegratedInterestsData(
                    emptyPage = EmptyPage(
                        title = "interestsEmptyTitle",
                        subtitle = "interestsEmptySubtitle",
                        image = "interestsEmpty",
                        topWidgetCollection = WidgetCollectionConfiguration.Instance(
                            widgets = listOf(
                                WidgetCollectionConfiguration.Instance.WidgetInfo(
                                    WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                    JsonArray(emptyList()),
                                )
                            )
                        )
                    )
                ),
                myFavorites = FavoriteConfig(
                    emptyPage = EmptyPage(
                        title = "favoritesEmptyTitle",
                        subtitle = "favoritesEmptySubtitle",
                        image = "favoritesEmpty",
                    )
                )
            )
            testFunction(
                selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests),
                hasInterests = true,
                expectedResult = ViewState.ContentState.Empty(
                    title = "favoritesEmptyTitle",
                    subtitle = "favoritesEmptySubtitle",
                    imageName = "favoritesEmpty",
                    widgets = null,
                    screenName = "screenName"
                )
            )
        }

        @Test
        fun `with myFavorites and myInterests and no interests`() {
            initialData = initialData.copy(
                myInterests = IntegratedInterestsData(
                    emptyPage = EmptyPage(
                        title = "interestsEmptyTitle",
                        subtitle = "interestsEmptySubtitle",
                        image = "interestsEmpty",
                        topWidgetCollection = WidgetCollectionConfiguration.Instance(
                            widgets = listOf(
                                WidgetCollectionConfiguration.Instance.WidgetInfo(
                                    WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                                    JsonArray(emptyList()),
                                )
                            )
                        )
                    )
                ),
                myFavorites = FavoriteConfig(
                    emptyPage = EmptyPage(
                        title = "favoritesEmptyTitle",
                        subtitle = "favoritesEmptySubtitle",
                        image = "favoritesEmpty",
                    )
                )
            )
            testFunction(
                selectedList = setOf(SelectedList.MyFavorites, SelectedList.MyInterests),
                hasInterests = false,
                expectedResult = ViewState.ContentState.Empty(
                    title = "interestsEmptyTitle",
                    subtitle = "interestsEmptySubtitle",
                    imageName = "interestsEmpty",
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
        }

        private fun testFunction(
            selectedList: Set<SelectedList> = emptySet(),
            hasInterests: Boolean = false,
            expectedResult: ViewState.ContentState.Empty,
        ) {
            expectedResult shouldBe getEmptyState(
                selectedList, initialData, hasInterests
            )
        }
    }

    private fun ListProvider.Element.toListViewDataItem(
        favIcon: FavoritesEditing.Icon? = null,
        tapAction: (Any, String) -> ListAction,
    ): ListViewData.ListItem {

        return ListViewData.ListItem(
            id = id,
            title = title,
            subtitle = subtitle,
            image = image,
            order = order,
            favIcon = favIcon?.icon,
            favIconDescription = favIcon?.accessibilityLabel?.let { it },
            onFavoriteTapAction = tapAction(id, title)
        )
    }

    private fun LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>.toHolder(id: Int) =
        with(this[id].assertNotNull()) {
            ListViewData.WidgetItem(id, MockWidgetGenerator(id.toString()))
        }
}
