package com.greencopper.event.activity.viewmodel

import androidx.fragment.app.DialogFragment
import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.ui.activitylist.ActivitiesListItem
import com.greencopper.eventmocks.MockActivityRepository
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.*
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.lists.ListViewModel
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.Locale

internal class ActivitiesListViewModelTest : CoroutineTest(StandardTestDispatcher()) {

    private lateinit var classUnderTest: ActivitiesListViewModel
    private lateinit var activityRepository: MockActivityRepository
    private lateinit var mockFavoritesManager: FavoritesManager<Long>

    private val widgetResolver = MockWidgetResolver()

    private val widgetCollectionConfigurationInstance = WidgetCollectionConfiguration.Instance(
        widgets = listOf(
            WidgetCollectionConfiguration.Instance.WidgetInfo(
                WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
                JsonArray(emptyList()),
            )
        ),
    )

    private val localizationService = MockLocalizationService()

    private val filteringHandler = MockFilteringHandler(
        _mockedPredicate = MockFilteringPredicateComputed(
            queryPattern = FilteringPredicate.Tag("tag").query()?.toSQL() ?: "",
            predicateResult = true
        ),
        mockedFilteringBarData = FilteringBarData(emptyList())
    )

    @Nested
    @DisplayName("Given activities without order")
    inner class ActivitiesWithoutOrder {

        private val activitiesWithoutOrder = listOf(
            ContentActivity(
                1,
                "name",
                "subtitle",
                "description",
                listOf("photo"),
                listOf("tag"),
            ),
            ContentActivity(
                2,
                "name",
                "subtitle",
                "description",
                listOf("photo"),
                listOf("tag2"),
            ),
            ContentActivity(
                3,
                "A-name",
                "subtitle",
                "description",
                listOf("photo"),
                listOf("tag"),
            ),
        )

        @BeforeEach
        internal fun setUp() {
            activityRepository = MockActivityRepository(
                activities = activitiesWithoutOrder,
                activitiesWithPredicate = activitiesWithoutOrder.minus(activitiesWithoutOrder[1])
            )
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = ActivitiesListViewModel(
                activityRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, Then it should return sorted activities with a widget collection")
        fun getItemsWithAWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance),
                        WidgetCollectionCellLayoutData(10, widgetCollectionConfigurationInstance)
                    )
                ).first()

                val expectedResult = listOf(
                    ActivitiesListItem.Card(
                        itemId = 3,
                        name = "A-name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                    ActivitiesListItem.WidgetCollectionHolder(
                        key = 1,
                        widgets = listOf(
                            WidgetCollectionView.WidgetItem(
                                key = WidgetCollectionConfiguration.Instance.WidgetKey(
                                    name = "testKey",
                                    version = 1
                                ),
                                params = MockWidgetParameters()
                            )
                        )
                    ),
                    ActivitiesListItem.Card(
                        itemId = 1,
                        name = "name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                    ActivitiesListItem.WidgetCollectionHolder(
                        key = 10,
                        widgets = listOf(
                            WidgetCollectionView.WidgetItem(
                                key = WidgetCollectionConfiguration.Instance.WidgetKey(
                                    name = "testKey",
                                    version = 1
                                ),
                                params = MockWidgetParameters()
                            )
                        )
                    ),
                )

                assertThat(resultList)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult)

                val resultListSecondTime = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance)
                    )
                ).first()

                // check the exactly same
                assertThat(resultListSecondTime)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult)

                // check widgets instance the same second time
                assertThat(
                    (resultList[1] as ActivitiesListItem.WidgetCollectionHolder).widgets ===
                            (resultListSecondTime[1] as ActivitiesListItem.WidgetCollectionHolder).widgets
                )
                    .isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems with an invalid widget collection, Then it should return sorted activities without a widget collection")
        fun getItemsWithInvalidWidgetCollectionShouldSucceed() {
            runTest {
                widgetResolver.widgetParameters = null
                val resultList2 = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(
                            1,
                            WidgetCollectionConfiguration.Instance(
                                widgets = emptyList()
                            )
                        )
                    )
                ).first()
                assertThat(resultList2).usingRecursiveComparison().isEqualTo(
                    listOf(
                        ActivitiesListItem.Card(
                            itemId = 3,
                            name = "A-name",
                            subtitle = "subtitle",
                            photo = "photo",
                            order = null,
                            isFavorite = false
                        ),
                        ActivitiesListItem.Card(
                            itemId = 1,
                            name = "name",
                            subtitle = "subtitle",
                            photo = "photo",
                            order = null,
                            isFavorite = false,
                        ),
                    )
                )
            }
        }

        @Test
        @DisplayName("When calling getItems without a widget collection, Then it should return sorted activities without a widget collection")
        fun getItemsWithoutWidgetCollectionsShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    ActivitiesListItem.Card(
                        itemId = 3,
                        name = "A-name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                    ActivitiesListItem.Card(
                        itemId = 1,
                        name = "name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                )

                // null
                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)

                //emptyList
                val resultList2 = classUnderTest.getItems(emptyList()).first()
                assertThat(resultList2).usingRecursiveComparison().isEqualTo(expectedResult)

                // with WidgetCollectionCellLayoutData with empty widgets
                val resultList3 = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(
                            1,
                            WidgetCollectionConfiguration.Instance(
                                widgets = emptyList()
                            )
                        )
                    )
                ).first()
                assertThat(resultList3).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }

        @Test
        @DisplayName("When calling getFilteringBarData, Then it should succeed")
        fun getFilteringBarDataShouldSucceed() {
            filteringHandler.mockedFilteringBarData = FilteringBarData(emptyList())
            val filteringBarData = classUnderTest.getFilteringBarData(DialogFragment(), "test")
            assertThat(filteringBarData.filters).isEmpty()
        }

        @Test
        @DisplayName("When calling getCurrentFilterState, Then it should succeed")
        fun getCurrentFilterStateShouldSucceed() {
            val result = classUnderTest.getCurrentFilterState()
            assertThat(result.mode).isEqualTo(Mode.DEFAULT)
            assertThat(classUnderTest.getCurrentFilterState().filteringInfo).isNull()

            val testFilteringInfo = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            )
            val savedFiltering = ListViewModel.SavedFiltering(
                Mode.DEFAULT,
                testFilteringInfo
            )
            filteringHandler.mockedCurrentStateToInfo = testFilteringInfo
            assertThat(classUnderTest.getCurrentFilterState())
                .usingRecursiveComparison()
                .isEqualTo(savedFiltering)
        }

        @Test
        fun toggleModeShouldSwitchMode() {
            classUnderTest.switchMode(Mode.MY_FAVORITES)
            assertThat(filteringHandler.currentMode).isEqualTo(Mode.MY_FAVORITES)
        }

        @Test
        fun getMode_returnsFilteringMode() {
            filteringHandler.currentModeValue = FilteringHandler.Mode.MY_FAVORITES
            assertThat(classUnderTest.getMode()).isEqualTo(FilteringHandler.Mode.MY_FAVORITES)
        }
    }

    @Nested
    @DisplayName("Given activities with order")
    inner class ActivitiesWithOrder {

        private val activitiesWithOrder = listOf(
            ContentActivity(
                1,
                "name",
                "subtitle",
                "description",
                listOf("photo"),
                listOf("tag"),
                2
            ),
            ContentActivity(
                2,
                "name",
                "subtitle",
                "description",
                listOf("photo"),
                listOf("tag2"),
                3
            ),
            ContentActivity(
                3,
                "A-name",
                "subtitle",
                "description",
                listOf("photo"),
                listOf("tag"),
                1
            ),
        )

        @BeforeEach
        internal fun setUp() {
            activityRepository = MockActivityRepository(
                activities = activitiesWithOrder,
                activitiesWithPredicate = activitiesWithOrder.minus(activitiesWithOrder[1])
            )
            mockFavoritesManager = MockFavoritesManager()
            mockFavoritesManager.addToFavorites(MockFavoriteable(3))
            classUnderTest = ActivitiesListViewModel(
                activityRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, Then it should return sorted activities with a widget collection")
        fun getItemsWithAWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance)
                    )
                ).first()

                val expectedResult = listOf(
                    ActivitiesListItem.Card(
                        itemId = 3,
                        name = "A-name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = 1,
                        isFavorite = true,
                    ),
                    ActivitiesListItem.WidgetCollectionHolder(
                        key = 1,
                        widgets = listOf(
                            WidgetCollectionView.WidgetItem(
                                key = WidgetCollectionConfiguration.Instance.WidgetKey(
                                    name = "testKey",
                                    version = 1
                                ),
                                params = MockWidgetParameters()
                            )
                        )
                    ),
                    ActivitiesListItem.Card(
                        itemId = 1,
                        name = "name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = 2,
                        isFavorite = false,
                    ),
                )

                assertThat(resultList)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult)

                val resultListSecondTime = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance)
                    )
                ).first()

                // check the exactly same
                assertThat(resultListSecondTime)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult)

                // check widgets instance the same second time
                assertThat(
                    (resultList[1] as ActivitiesListItem.WidgetCollectionHolder).widgets ===
                            (resultListSecondTime[1] as ActivitiesListItem.WidgetCollectionHolder).widgets
                )
                    .isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems with an invalid widget collection, Then it should return sorted activities without a widget collection")
        fun getItemsWithInvalidWidgetCollectionShouldSucceed() {
            runTest {
                widgetResolver.widgetParameters = null
                val resultList2 = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(
                            1,
                            WidgetCollectionConfiguration.Instance(
                                widgets = emptyList()
                            )
                        )
                    )
                ).first()
                assertThat(resultList2).usingRecursiveComparison().isEqualTo(
                    listOf(
                        ActivitiesListItem.Card(
                            itemId = 3,
                            name = "A-name",
                            subtitle = "subtitle",
                            photo = "photo",
                            order = 1,
                            isFavorite = true,
                        ),
                        ActivitiesListItem.Card(
                            itemId = 1,
                            name = "name",
                            subtitle = "subtitle",
                            photo = "photo",
                            order = 2,
                            isFavorite = false,
                        ),
                    )
                )
            }
        }

        @Test
        @DisplayName("When calling getItems without a widget collection, Then it should return sorted activities without a widget collection")
        fun getItemsWithoutWidgetCollectionsShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    ActivitiesListItem.Card(
                        itemId = 3,
                        name = "A-name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = 1,
                        isFavorite = true,
                    ),
                    ActivitiesListItem.Card(
                        itemId = 1,
                        name = "name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = 2,
                        isFavorite = false,
                    ),
                )

                // null
                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }

        @Test
        @DisplayName("When calling getItems in favorite mode, Then it should return 1 activity")
        fun getItemsInFavoriteModeShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    ActivitiesListItem.Card(
                        itemId = 3,
                        name = "A-name",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = 1,
                        isFavorite = true,
                    ),
                )

                classUnderTest.switchMode(Mode.MY_FAVORITES)
                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }

        @Test
        @DisplayName("When calling getFilteringBarData, Then it should succeed")
        fun getFilteringBarDataShouldSucceed() {
            filteringHandler.mockedFilteringBarData = FilteringBarData(emptyList())
            val filteringBarData = classUnderTest.getFilteringBarData(DialogFragment(), "test")
            assertThat(filteringBarData.filters).isEmpty()
        }

        @Test
        @DisplayName("When calling getCurrentFilterState, Then it should succeed")
        fun getCurrentFilterStateShouldSucceed() {
            val result = classUnderTest.getCurrentFilterState()
            assertThat(result.mode).isEqualTo(Mode.DEFAULT)
            assertThat(classUnderTest.getCurrentFilterState().filteringInfo).isNull()

            val testFilteringInfo = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            )
            val savedFiltering = ListViewModel.SavedFiltering(
                Mode.DEFAULT,
                testFilteringInfo
            )
            filteringHandler.mockedCurrentStateToInfo = testFilteringInfo
            assertThat(classUnderTest.getCurrentFilterState())
                .usingRecursiveComparison()
                .isEqualTo(savedFiltering)
        }

        @Test
        @DisplayName("When calling toggleFavoriteMode, Then mode should be my favorites")
        fun toggleModeShouldSwitchMode() {
            classUnderTest.switchMode(Mode.MY_FAVORITES)
            assertThat(filteringHandler.currentMode).isEqualTo(Mode.MY_FAVORITES)
        }
    }

    @Nested
    @DisplayName("Given activities with name")
    inner class ActivitiesWithName {
        private val activitiesWithName = listOf(
            ContentActivity(1, "BBB", "", "", emptyList(), emptyList(), null),
            ContentActivity(2, "AAA", "", "", emptyList(), emptyList(), null),
            ContentActivity(3, "aaa", "", "", emptyList(), emptyList(), null),
        )

        @BeforeEach
        internal fun setUp() {
            activityRepository = MockActivityRepository(
                activities = activitiesWithName,
                activitiesWithPredicate = activitiesWithName
            )
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = ActivitiesListViewModel(
                activityRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems, Then it should return Activities sorted with capitalized letters first")
        fun getItemsShouldSortAndSucceed() {
            runTest {
                val expectedResult = listOf(
                    ActivitiesListItem.Card(2, "AAA", "", null, null, false),
                    ActivitiesListItem.Card(3, "aaa", "", null, null, false),
                    ActivitiesListItem.Card(1, "BBB", "", null, null, false),
                )

                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }
    }

    @Nested
    @DisplayName("Given no activities are found")
    inner class NoActivities {

        @BeforeEach
        internal fun setUp() {
            activityRepository = MockActivityRepository()
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = ActivitiesListViewModel(
                activityRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, then no activities should be found")
        fun getItemsWithWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance)
                    )
                ).first()
                assertThat(resultList.isEmpty()).isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems without a widget collection, then no activities should be found")
        fun getItemsWithoutWidgetCollectionShouldSucceed() {
            runTest {
                val resultList2 = classUnderTest.getItems(null).first()
                assertThat(resultList2.isEmpty()).isTrue
            }
        }
    }

    @Nested
    inner class UnrelatedToActivities {

        @BeforeEach
        internal fun setUp() {
            activityRepository = MockActivityRepository()
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = ActivitiesListViewModel(
                activityRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getFilteringBarData, Then it should succeed")
        fun getFilteringBarDataShouldSucceed() {
            filteringHandler.mockedFilteringBarData = FilteringBarData(emptyList())
            val filteringBarData = classUnderTest.getFilteringBarData(DialogFragment(), "test")
            assertThat(filteringBarData.filters).isEmpty()
        }

        @Test
        @DisplayName("When calling getCurrentFilterState, Then it should succeed")
        fun getCurrentFilterStateShouldSucceed() {
            val result = classUnderTest.getCurrentFilterState()
            assertThat(result.mode).isEqualTo(Mode.DEFAULT)
            assertThat(classUnderTest.getCurrentFilterState().filteringInfo).isNull()

            val testFilteringInfo = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            )
            val savedFiltering = ListViewModel.SavedFiltering(
                Mode.DEFAULT,
                testFilteringInfo
            )
            filteringHandler.mockedCurrentStateToInfo = testFilteringInfo
            assertThat(classUnderTest.getCurrentFilterState())
                .usingRecursiveComparison()
                .isEqualTo(savedFiltering)
        }

        @Test
        @DisplayName("When calling toggleFavoriteMode, Then mode should be default")
        fun toggleModeShouldSwitchMode() {
            classUnderTest.switchMode(Mode.DEFAULT)
            assertThat(filteringHandler.currentMode).isEqualTo(Mode.DEFAULT)
        }

        @Test
        fun whenChangingFilter_shouldNotifiy() {
            var count = 0

            classUnderTest = ActivitiesListViewModel(
                activityRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )

            runTest {
                filteringHandler.switchMode(Mode.MY_FAVORITES)

                val job = launch {
                    classUnderTest.filterChangingNotifier.collectLatest {
                        count++
                    }
                }

                delay(500)
                filteringHandler.switchMode(Mode.DEFAULT)
                delay(500)

                job.cancel()
            }
            assertThat(count).isEqualTo(1)
        }
    }

    override fun afterEach() {}
}
