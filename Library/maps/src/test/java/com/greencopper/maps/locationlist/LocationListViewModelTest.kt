package com.greencopper.maps.locationlist

import androidx.fragment.app.DialogFragment
import com.greencopper.interfacekit.filtering.*
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.maps.common.LocationData
import com.greencopper.maps.locationlist.ui.LocationListItem
import com.greencopper.mapsmocks.MockMapsRepository
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

internal class LocationListViewModelTest : CoroutineTest(StandardTestDispatcher()) {

    private lateinit var classUnderTest: LocationListViewModel
    private lateinit var mapsRepository: MockMapsRepository
    private lateinit var mockFavoritesManager: MockFavoritesManager<String>
    private val widgetResolver = MockWidgetResolver()
    private val localizationService = MockLocalizationService()

    private val widgetCollectionConfigurationInstance = WidgetCollectionConfiguration.Instance(
        widgets = listOf(
            WidgetCollectionConfiguration.Instance.WidgetInfo(
                WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
                JsonArray(emptyList()),
            )
        ),
    )

    private val filteringHandler = MockFilteringHandler(
        _mockedPredicate = MockFilteringPredicateComputed(
            queryPattern = FilteringPredicate.Tag("tag").query()?.toSQL() ?: "",
            predicateResult = true
        ),
        mockedFilteringBarData = FilteringBarData(emptyList())
    )

    @Nested
    @DisplayName("Given activities without order")
    inner class LocationsWithoutOrder {

        private val locationsWithoutOrder = listOf(
            LocationData(
                itemId = "3",
                name = "3",
                tags = listOf("tag", "tag32")
            ),
            LocationData(
                itemId = "2",
                name = "2",
                tags = listOf("tag21", "tag22")
            ),
            LocationData(
                itemId = "1",
                name = "1",
                tags = listOf("tag", "tag12")
            ),
        )

        @BeforeEach
        internal fun setUp() {
            mapsRepository = MockMapsRepository(
                rearrangedLocationsWithPredicate = locationsWithoutOrder.minus(locationsWithoutOrder[1]),
                rearrangedLocations = locationsWithoutOrder
            )

            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = LocationListViewModel(
                mapsRepository = mapsRepository,
                filteringHandler = filteringHandler,
                widgetResolver = widgetResolver,
                localization = localizationService,
                myLocationsManager = mockFavoritesManager,
                locale = Locale.getDefault()
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, Then it should return sorted locations with a widget collection")
        fun getItemsWithAWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance),
                        WidgetCollectionCellLayoutData(10, widgetCollectionConfigurationInstance)
                    )
                ).first()

                val expectedResult = listOf(
                    LocationListItem.LocationItem(
                        itemId = "1",
                        name = "1",
                        subtitle = null,
                        photo = null,
                        isFavorite = false,
                        order = null,
                    ),
                    LocationListItem.WidgetCollectionHolder(
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
                    LocationListItem.LocationItem(
                        itemId = "3",
                        name = "3",
                        subtitle = null,
                        photo = null,
                        isFavorite = false,
                        order = null,
                    ),
                    LocationListItem.WidgetCollectionHolder(
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
                    (resultList[1] as LocationListItem.WidgetCollectionHolder).widgets ===
                            (resultListSecondTime[1] as LocationListItem.WidgetCollectionHolder).widgets
                )
                    .isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems with an invalid widget collection, Then it should return sorted locations without a widget collection")
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
                        LocationListItem.LocationItem(
                            itemId = "1",
                            name = "1",
                            subtitle = null,
                            photo = null,
                            isFavorite = false,
                            order = null,
                        ),
                        LocationListItem.LocationItem(
                            itemId = "3",
                            name = "3",
                            subtitle = null,
                            photo = null,
                            isFavorite = false,
                            order = null,
                        ),
                    )
                )
            }
        }

        @Test
        @DisplayName("When calling getItems without a widget collection, Then it should return sorted locations without a widget collection")
        fun getItemsWithoutWidgetCollectionsShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    LocationListItem.LocationItem(
                        itemId = "1",
                        name = "1",
                        subtitle = null,
                        photo = null,
                        isFavorite = false,
                        order = null,
                    ),
                    LocationListItem.LocationItem(
                        itemId = "3",
                        name = "3",
                        subtitle = null,
                        photo = null,
                        isFavorite = false,
                        order = null,
                    ),
                )

                // null
                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)

                //emptyList
                val resultList2 =
                    classUnderTest.getItems(emptyList()).first()
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
            assertThat(result.mode).isEqualTo(FilteringHandler.Mode.DEFAULT)
            assertThat(classUnderTest.getCurrentFilterState().filteringInfo).isNull()

            val testFilteringInfo = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            )
            val savedFiltering = LocationListViewModel.SavedFiltering(
                FilteringHandler.Mode.DEFAULT,
                testFilteringInfo
            )
            filteringHandler.mockedCurrentStateToInfo = testFilteringInfo
            assertThat(classUnderTest.getCurrentFilterState())
                .usingRecursiveComparison()
                .isEqualTo(savedFiltering)
        }

        @Test
        fun toggleModeShouldSwitchMode() {
            classUnderTest.switchMode(FilteringHandler.Mode.MY_FAVORITES)
            assertThat(filteringHandler.currentMode).isEqualTo(FilteringHandler.Mode.MY_FAVORITES)
        }
    }


    @Nested
    @DisplayName("Given locations with name")
    inner class LocationsWithName {
        private val namedLocations = listOf(
            LocationData("3", "aaa", tags = emptyList(), order = null),
            LocationData("6", "CCC", tags = emptyList(), order = 5),
            LocationData("5", "BBB", tags = emptyList(), order = 3),
            LocationData("1", "AAA", tags = emptyList(), order = null),
            LocationData("12", "ddd", tags = emptyList(), order = 4),
        )

        @BeforeEach
        internal fun setUp() {
            mapsRepository = MockMapsRepository(
                rearrangedLocations = namedLocations,
                rearrangedLocationsWithPredicate = namedLocations,
            )

            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = LocationListViewModel(
                mapsRepository = mapsRepository,
                filteringHandler = filteringHandler,
                widgetResolver = widgetResolver,
                localization = localizationService,
                myLocationsManager = mockFavoritesManager,
                locale = Locale.getDefault()
            )
        }

        @Test
        @DisplayName("When calling getItems, Then it should return Locations sorted with capitalized letters first")
        fun getItemsShouldSortAndSucceed() {
            runTest {
                val expectedResult = listOf(
                    LocationListItem.LocationItem("5", "BBB", null, null, false, 3),
                    LocationListItem.LocationItem("12", "ddd", null, null, false, 4),
                    LocationListItem.LocationItem("6", "CCC", null, null, false, 5),
                    LocationListItem.LocationItem("3", "aaa", null, null, false, null),
                    LocationListItem.LocationItem("1", "AAA", null, null, false, null),
                )

                // null
                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }
    }

    @Nested
    @DisplayName("Given locations with order")
    inner class LocationsWithOrder {

        private val locationsWithOrder = listOf(
            LocationData(
                itemId = "3",
                name = "3",
                tags = listOf("tag", "tag32"),
                order = 1,
            ),
            LocationData(
                itemId = "2",
                name = "2",
                tags = listOf("tag21", "tag22"),
                order = null,
            ),
            LocationData(
                itemId = "1",
                name = "1",
                tags = listOf("tag", "tag12"),
                order = 3,
            ),
        )


        @BeforeEach
        internal fun setUp() {
            mapsRepository = MockMapsRepository(
                rearrangedLocationsWithPredicate = locationsWithOrder.minus(locationsWithOrder[1]),
                rearrangedLocations = locationsWithOrder
            )

            mockFavoritesManager = MockFavoritesManager()
            mockFavoritesManager.addToFavorites(MockFavoriteable("3"))
            classUnderTest = LocationListViewModel(
                mapsRepository = mapsRepository,
                filteringHandler = filteringHandler,
                widgetResolver = widgetResolver,
                localization = localizationService,
                myLocationsManager = mockFavoritesManager,
                locale = Locale.getDefault()
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, Then it should return sorted locations with a widget collection")
        fun getItemsWithAWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance)
                    )
                ).first()

                val expectedResult = listOf(
                    LocationListItem.LocationItem(
                        itemId = "3",
                        name = "3",
                        subtitle = null,
                        photo = null,
                        isFavorite = true,
                        order = 1,
                    ),
                    LocationListItem.WidgetCollectionHolder(
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
                    LocationListItem.LocationItem(
                        itemId = "1",
                        name = "1",
                        subtitle = null,
                        photo = null,
                        isFavorite = false,
                        order = 3,
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
                    (resultList[1] as LocationListItem.WidgetCollectionHolder).widgets ===
                            (resultListSecondTime[1] as LocationListItem.WidgetCollectionHolder).widgets
                )
                    .isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems with an invalid widget collection, Then it should return sorted locations without a widget collection")
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
                        LocationListItem.LocationItem(
                            itemId = "3",
                            name = "3",
                            subtitle = null,
                            photo = null,
                            isFavorite = true,
                            order = 1,
                        ),
                        LocationListItem.LocationItem(
                            itemId = "1",
                            name = "1",
                            subtitle = null,
                            photo = null,
                            isFavorite = false,
                            order = 3,
                        ),
                    )
                )
            }
        }

        @Test
        @DisplayName("When calling getItems without a widget collection, Then it should return sorted locations without a widget collection")
        fun getItemsWithoutWidgetCollectionsShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    LocationListItem.LocationItem(
                        itemId = "3",
                        name = "3",
                        subtitle = null,
                        photo = null,
                        isFavorite = true,
                        order = 1,
                    ),
                    LocationListItem.LocationItem(
                        itemId = "1",
                        name = "1",
                        subtitle = null,
                        photo = null,
                        isFavorite = false,
                        order = 3,
                    ),
                )

                // null
                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }

        @Test
        @DisplayName("When calling getItems in favorite mode, Then it should return 1 location")
        fun getItemsInFavoriteModeShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    LocationListItem.LocationItem(
                        itemId = "3",
                        name = "3",
                        subtitle = null,
                        photo = null,
                        isFavorite = true,
                        order = 1,
                    ),
                )

                classUnderTest.switchMode(FilteringHandler.Mode.MY_FAVORITES)
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
            assertThat(result.mode).isEqualTo(FilteringHandler.Mode.DEFAULT)
            assertThat(classUnderTest.getCurrentFilterState().filteringInfo).isNull()

            val testFilteringInfo = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            )
            val savedFiltering = LocationListViewModel.SavedFiltering(
                FilteringHandler.Mode.DEFAULT,
                testFilteringInfo
            )
            filteringHandler.mockedCurrentStateToInfo = testFilteringInfo
            assertThat(classUnderTest.getCurrentFilterState())
                .usingRecursiveComparison()
                .isEqualTo(savedFiltering)
        }

        @Test
        @DisplayName("When calling switchMode, Then mode should be my favorites")
        fun toggleModeShouldSwitchMode() {
            classUnderTest.switchMode(FilteringHandler.Mode.MY_FAVORITES)
            assertThat(filteringHandler.currentMode).isEqualTo(FilteringHandler.Mode.MY_FAVORITES)
        }
    }

    @Nested
    @DisplayName("Given no locations are found")
    inner class NoLocations {

        @BeforeEach
        internal fun setUp() {
            mapsRepository = MockMapsRepository()

            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = LocationListViewModel(
                mapsRepository = mapsRepository,
                filteringHandler = filteringHandler,
                widgetResolver = widgetResolver,
                localization = localizationService,
                myLocationsManager = mockFavoritesManager,
                locale = Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, then no location should be found")
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
        @DisplayName("When calling getItems without a widget collection, then no location should be found")
        fun getItemsWithoutWidgetCollectionShouldSucceed() {
            runTest {
                val resultList2 = classUnderTest.getItems(null).first()
                assertThat(resultList2.isEmpty()).isTrue
            }
        }
    }

    @Nested
    inner class UnrelatedToLocations {

        @BeforeEach
        internal fun setUp() {
            mapsRepository = MockMapsRepository()
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = LocationListViewModel(
                mapsRepository = mapsRepository,
                filteringHandler = filteringHandler,
                widgetResolver = widgetResolver,
                localization = localizationService,
                myLocationsManager = mockFavoritesManager,
                locale = Locale.getDefault(),
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
            assertThat(result.mode).isEqualTo(FilteringHandler.Mode.DEFAULT)
            assertThat(classUnderTest.getCurrentFilterState().filteringInfo).isNull()

            val testFilteringInfo = FilteringInfo(
                predicate = FilteringPredicate.Tag("tag")
            )
            val savedFiltering = LocationListViewModel.SavedFiltering(
                FilteringHandler.Mode.DEFAULT,
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
            classUnderTest.switchMode(FilteringHandler.Mode.DEFAULT)
            assertThat(filteringHandler.currentMode).isEqualTo(FilteringHandler.Mode.DEFAULT)
        }

        @Test
        fun whenChangingFilter_shouldNotifiy() {
            var count = 0

            classUnderTest = LocationListViewModel(
                mapsRepository = mapsRepository,
                filteringHandler = filteringHandler,
                widgetResolver = widgetResolver,
                localization = localizationService,
                myLocationsManager = mockFavoritesManager,
                locale = Locale.getDefault(),
            )

            runTest {
                filteringHandler.switchMode(FilteringHandler.Mode.MY_FAVORITES)

                val job = launch {
                    classUnderTest.filterChangingNotifier.collectLatest {
                        count++
                    }
                }

                delay(500)
                filteringHandler.switchMode(FilteringHandler.Mode.DEFAULT)
                delay(500)

                job.cancel()
            }
            assertThat(count).isEqualTo(1)
        }
    }

    override fun afterEach() {}
}
