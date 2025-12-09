package com.greencopper.event.performers.viewmodel

import androidx.fragment.app.DialogFragment
import com.greencopper.event.performers.Performer
import com.greencopper.event.performers.ui.performerslist.PerformersListItem
import com.greencopper.eventmocks.MockPerformerRepository
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

internal class PerformersListViewModelTest : CoroutineTest(StandardTestDispatcher()) {

    private lateinit var classUnderTest: PerformersListViewModel
    private lateinit var performersRepository: MockPerformerRepository
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
    @DisplayName("Given performers without order")
    inner class PerformersWithoutOrder {

        private val performersWithoutOrder = listOf(
            Performer(
                itemId = "1",
                name = "Men I Trust",
                subtitle = "subtitle",
                description = null,
                photos = listOf("photo"),
                tags = listOf("tag")
            ),
            Performer(
                itemId = "2",
                name = "Emma",
                photos = listOf("photo"),
                tags = listOf("tag2")
            ),
            Performer(
                itemId = "3",
                name = "King Gizzard",
                photos = listOf("photo"),
                tags = listOf("tag")
            )
        )

        @BeforeEach
        internal fun setUp() {
            performersRepository = MockPerformerRepository(
                performersWithPredicate = performersWithoutOrder.minus(performersWithoutOrder[1])
            )

            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = PerformersListViewModel(
                performersRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, Then it should return sorted performers with a widget collection")
        fun getItemsWithAWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance),
                        WidgetCollectionCellLayoutData(10, widgetCollectionConfigurationInstance)
                    )
                ).first()

                val expectedResult = listOf(
                    PerformersListItem.Card(
                        itemId = "3",
                        name = "King Gizzard",
                        subtitle = null,
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                    PerformersListItem.WidgetCollectionHolder(
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
                    PerformersListItem.Card(
                        itemId = "1",
                        name = "Men I Trust",
                        subtitle = "subtitle",
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                    PerformersListItem.WidgetCollectionHolder(
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
                    (resultList[1] as PerformersListItem.WidgetCollectionHolder).widgets ===
                            (resultListSecondTime[1] as PerformersListItem.WidgetCollectionHolder).widgets
                )
                    .isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems with an invalid widget collection, Then it should return sorted performers without a widget collection")
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
                        PerformersListItem.Card(
                            itemId = "3",
                            name = "King Gizzard",
                            subtitle = null,
                            photo = "photo",
                            order = null,
                            isFavorite = false
                        ),
                        PerformersListItem.Card(
                            itemId = "1",
                            name = "Men I Trust",
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
        @DisplayName("When calling getItems without a widget collection, Then it should return sorted performers without a widget collection")
        fun getItemsWithoutWidgetCollectionsShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    PerformersListItem.Card(
                        itemId = "3",
                        name = "King Gizzard",
                        subtitle = null,
                        photo = "photo",
                        order = null,
                        isFavorite = false,
                    ),
                    PerformersListItem.Card(
                        itemId = "1",
                        name = "Men I Trust",
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
    }

    @Nested
    @DisplayName("Given performers with order")
    inner class PerformersWithOrder {

        private val performersWithOrder = listOf(
            Performer(
                itemId = "1",
                name = "Men I Trust",
                subtitle = "subtitle",
                description = null,
                photos = listOf("photo"),
                tags = listOf("tag"),
                order = 2
            ),
            Performer(
                itemId = "2",
                name = "Emma",
                photos = listOf("photo"),
                tags = listOf("tag2"),
                order = 3
            ),
            Performer(
                itemId = "3",
                name = "King Gizzard",
                photos = listOf("photo"),
                tags = listOf("tag"),
                order = 1
            )
        )


        @BeforeEach
        internal fun setUp() {
            performersRepository = MockPerformerRepository(
                performers = performersWithOrder,
                performersWithPredicate = performersWithOrder.minus(performersWithOrder[1]),
            )

            mockFavoritesManager = MockFavoritesManager()
            mockFavoritesManager.addToFavorites(MockFavoriteable("3"))
            classUnderTest = PerformersListViewModel(
                performersRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, Then it should return sorted performers with a widget collection")
        fun getItemsWithAWidgetCollectionShouldSucceed() {
            runTest {
                val resultList = classUnderTest.getItems(
                    listOf(
                        WidgetCollectionCellLayoutData(1, widgetCollectionConfigurationInstance)
                    )
                ).first()

                val expectedResult = listOf(
                    PerformersListItem.Card(
                        itemId = "3",
                        name = "King Gizzard",
                        subtitle = null,
                        photo = "photo",
                        order = 1,
                        isFavorite = true,
                    ),
                    PerformersListItem.WidgetCollectionHolder(
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
                    PerformersListItem.Card(
                        itemId = "1",
                        name = "Men I Trust",
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
                    (resultList[1] as PerformersListItem.WidgetCollectionHolder).widgets ===
                            (resultListSecondTime[1] as PerformersListItem.WidgetCollectionHolder).widgets
                )
                    .isTrue
            }
        }

        @Test
        @DisplayName("When calling getItems with an invalid widget collection, Then it should return sorted performers without a widget collection")
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
                        PerformersListItem.Card(
                            itemId = "3",
                            name = "King Gizzard",
                            subtitle = null,
                            photo = "photo",
                            order = 1,
                            isFavorite = true,
                        ),
                        PerformersListItem.Card(
                            itemId = "1",
                            name = "Men I Trust",
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
        @DisplayName("When calling getItems without a widget collection, Then it should return sorted performers without a widget collection")
        fun getItemsWithoutWidgetCollectionsShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    PerformersListItem.Card(
                        itemId = "3",
                        name = "King Gizzard",
                        subtitle = null,
                        photo = "photo",
                        order = 1,
                        isFavorite = true,
                    ),
                    PerformersListItem.Card(
                        itemId = "1",
                        name = "Men I Trust",
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
        @DisplayName("When calling getItems in favorite mode, Then it should return 1 performer")
        fun getItemsInFavoriteModeShouldSucceed() {
            runTest {
                val expectedResult = listOf(
                    PerformersListItem.Card(
                        itemId = "3",
                        name = "King Gizzard",
                        subtitle = null,
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
    @DisplayName("Given performers with name")
    inner class PerformersWithName {
        private val performersWithName = listOf(
            Performer("3", "aaa", null, null, null, emptyList(), emptyList()),
            Performer("1", "AAA", null, null, null, emptyList(), emptyList()),
            Performer("2", "BBB", null, null, null, emptyList(), emptyList()),
        )

        @BeforeEach
        internal fun setUp() {
            performersRepository = MockPerformerRepository(
                performers = performersWithName,
                performersWithPredicate = performersWithName
            )
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = PerformersListViewModel(
                performersRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems, Then it should return Performers sorted with capitalized letters first")
        fun getItemsShouldSortAndSucceed() {
            runTest {
                val expectedResult = listOf(
                    PerformersListItem.Card("3", "aaa", null, null, null, false),
                    PerformersListItem.Card("1", "AAA", null, null, null, false),
                    PerformersListItem.Card("2", "BBB", null, null, null, false),
                )

                val resultList = classUnderTest.getItems(null).first()
                assertThat(resultList).usingRecursiveComparison().isEqualTo(expectedResult)
            }
        }
    }

    @Nested
    @DisplayName("Given no performers are found")
    inner class NoPerformers {

        @BeforeEach
        internal fun setUp() {
            performersRepository = MockPerformerRepository()

            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = PerformersListViewModel(
                performersRepository,
                filteringHandler,
                widgetResolver,
                localizationService,
                mockFavoritesManager,
                Locale.getDefault(),
            )
        }

        @Test
        @DisplayName("When calling getItems with a widget collection, then no performers should be found")
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
        @DisplayName("When calling getItems without a widget collection, then no performers should be found")
        fun getItemsWithoutWidgetCollectionShouldSucceed() {
            runTest {
                val resultList2 = classUnderTest.getItems(null).first()
                assertThat(resultList2.isEmpty()).isTrue
            }
        }
    }

    @Nested
    inner class UnrelatedToPerformers {

        @BeforeEach
        internal fun setUp() {
            performersRepository = MockPerformerRepository()
            mockFavoritesManager = MockFavoritesManager()
            classUnderTest = PerformersListViewModel(
                performersRepository,
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

            classUnderTest = PerformersListViewModel(
                performersRepository,
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
