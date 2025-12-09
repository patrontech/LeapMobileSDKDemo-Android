package com.greencopper.interfacekit.filtering

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode.DEFAULT
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode.MY_FAVORITES
import com.greencopper.interfacekit.filtering.filterselector.FilterSelectorData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockAggregateMetricsService
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ConcreteFilteringHandlerTest {

    init {
        Toolkit.setupTest()
        bindSingleton<AggregateMetricsService>(MockAggregateMetricsService())
        bindProvider<LocalizationService>(MockLocalizationService())
    }

    //region Initialisation
    @Test
    fun initWithNull_stateShouldBeNull() {
        //given
        val handler = ConcreteFilteringHandler(filteringInfoMap = emptyMap())

        runTest {
            //then
            assertThat(handler.currentStateToInfo).isNull()
            assertThat(handler.predicate.first()).isNull()
            assertThat(handler.buildBarData(mockk(), "").filters).isEmpty()
            assertThat(handler.buildSelectorData().filters).isEmpty()
        }
    }

    @Test
    fun initWithEmptyFilters_stateShouldBeEmpty() {
        //given
        val predicate = FilteringPredicate.Tag("test")
        val filteringInfo = FilteringInfo(predicate, emptyMap())
        val handler = ConcreteFilteringHandler(
            filteringInfoMap = mapOf(DEFAULT to filteringInfo)
        )

        runTest {
            assertThat(handler.predicate.first()?.toSQL()).isEqualTo(predicate.query(emptyMap()).toSQL())
            assertThat(handler.buildBarData(mockk(), "").filters).isEmpty()
            assertThat(handler.buildSelectorData().filters).isEmpty()
        }
    }

    @Test
    fun initWithFilters_stateShouldReflect() {
        //given
        val json: Json = App.resolve()
        val predicate = json.decodeFromString<FilteringPredicate>("\"TAG tag1 AND FILTER filter1\"")
        val filteringInfo = FilteringInfo(
            predicate, mapOf(
                "filter1" to FilterInfo.CheckBox(
                    "checkBox1", FilteringPredicate.Operator.OR, listOf(
                        FilterInfo.CheckBox.Option(
                            "option11",
                            FilteringPredicate.Tag("option11tag"),
                            true
                        ),
                        FilterInfo.CheckBox.Option(
                            "option12",
                            FilteringPredicate.Tag("option12tag")
                        ),
                        FilterInfo.CheckBox.Option(
                            "option13",
                            FilteringPredicate.Tag("option13tag"),
                            true
                        )
                    ),
                    index = 0
                )
            )
        )
        val handler = ConcreteFilteringHandler(
            filteringInfoMap = mapOf(DEFAULT to filteringInfo)
        )

        runTest {
            assertThat(handler.predicate.first()?.toSQL()).isEqualTo(
                predicate.query(
                    FilteringState(
                        filteringInfo
                    ).filters
                )?.toSQL()
            )
            assertThat(handler.buildBarData(mockk(), "")).isNotNull
            assertThat(handler.buildSelectorData().filters).isNotEmpty
        }
    }
    //endregion

    @Nested
    inner class WithFiltersAndDefaultMode {

        private val filteringInfo: FilteringInfo
        private val handler: ConcreteFilteringHandler

        init {
            val json: Json = App.resolve()
            val predicate =
                json.decodeFromString<FilteringPredicate>("\"TAG tag1 AND FILTER filter1 OR TAG tag2 OR FILTER filter2\"")
            filteringInfo = FilteringInfo(
                predicate, mapOf(
                    "filter1" to FilterInfo.CheckBox(
                        "checkBox1", FilteringPredicate.Operator.OR, listOf(
                            FilterInfo.CheckBox.Option(
                                "option11",
                                FilteringPredicate.Tag("option11tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option12",
                                FilteringPredicate.Tag("option12tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option13",
                                FilteringPredicate.Tag("option13tag")
                            )
                        ),
                        index = 1
                    ),
                    "filter2" to FilterInfo.CheckBox(
                        "checkBox2", FilteringPredicate.Operator.AND, listOf(
                            FilterInfo.CheckBox.Option(
                                "option21",
                                FilteringPredicate.Tag("option21tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option22",
                                FilteringPredicate.Tag("option22tag")
                            )
                        ),
                        index = 2
                    )
                )
            )
            handler = ConcreteFilteringHandler(
                filteringInfoMap = mapOf(DEFAULT to filteringInfo)
            )
        }

        @Test
        fun getQuery_withUnchangedState_shouldBeValid() {
            runTest {
                assertThat(
                    handler.predicate.first()?.toSQL()
                ).isEqualTo("(tags LIKE '%\"tag1\"%' OR tags LIKE '%\"tag2\"%')")
            }
        }

        @Test
        fun selectorData_shouldReflectState() {
            //given
            val filters = handler.buildSelectorData().filters
            val filterGroup = filters["filter1"]
            val title = filterGroup?.get(0) as FilterSelectorData.Cell.Title
            val cell = filterGroup[1] as FilterSelectorData.Cell.Option

            //then
            assertThat(filters.size).isEqualTo(2)
            assertThat(filterGroup.size).isEqualTo(4)
            assertThat(title.title).isEqualTo("checkBox1")
            assertThat(cell.label).isEqualTo("option11")
            assertThat(cell.isActive).isFalse
        }

        @Test
        fun tapOnCell_shouldUpdateState() {
            //when
            clickOnCell("filter1", 1)
            clickOnCell("filter1", 2)

            //then
            runTest {
                assertThat(handler.predicate.first()?.toSQL()).isEqualTo(
                    "(" +
                            "(tags LIKE '%\"tag1\"%' AND " +
                            "(tags LIKE '%\"option11tag\"%' OR tags LIKE '%\"option12tag\"%')) OR " +
                            "tags LIKE '%\"tag2\"%')"
                )
            }
            assertThat(
                (handler.currentStatesToInfoMap[DEFAULT]?.filters?.get("filter1") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isTrue
            assertThat(
                (handler.currentStatesToInfoMap[DEFAULT]?.filters?.get("filter1") as? FilterInfo.CheckBox)?.options?.get(
                    1
                )?.isActive
            ).isTrue
        }

        @Test
        fun barData_shouldReflectState() {
            //given
            val barData = handler.buildBarData(mockk(), "")
            val filteringBarCell = barData.filters[0]

            //then
            assertThat(barData.filters.size).isEqualTo(2)
            assertThat(filteringBarCell.isSelected).isFalse
            assertThat(filteringBarCell.name).isEqualTo("checkBox1")
        }

        @Test
        fun barData_cellName_shouldAdapt_toActiveFilters() {
            clickOnCell("filter1", 1)
            assertThat(
                handler.buildBarData(
                    mockk(),
                    ""
                ).filters[0].name
            ).isEqualTo("option11")

            clickOnCell("filter1", 2)
            assertThat(
                handler.buildBarData(
                    mockk(),
                    ""
                ).filters[0].name
            ).isEqualTo("option11, option12")

            clickOnCell("filter1", 3)
            assertThat(
                handler.buildBarData(
                    mockk(),
                    ""
                ).filters[0].name
            ).isEqualTo("checkBox1 (3)")
        }

        @Test
        fun clear_shouldResetFilters() {
            assertThat(
                (handler.currentStatesToInfoMap[DEFAULT]?.filters?.get("filter1") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isFalse
            clickOnCell("filter1", 1)
            assertThat(
                (handler.currentStatesToInfoMap[DEFAULT]?.filters?.get("filter1") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isTrue
            handler.buildSelectorData().onClearTap(listOf("filter1"))
            assertThat(
                (handler.currentStatesToInfoMap[DEFAULT]?.filters?.get("filter1") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isFalse
        }

        private fun clickOnCell(filterId: FilterId, index: Int) {
            (handler.buildSelectorData().filters[filterId]?.get(index) as FilterSelectorData.Cell.Option).onTap()
        }
    }

    @Nested
    inner class WithFiltersAndTwoModes {

        private val defaultFilteringInfo: FilteringInfo
        private val secondFilteringInfo: FilteringInfo
        private val handler: ConcreteFilteringHandler

        init {
            val json: Json = App.resolve()
            val predicate =
                json.decodeFromString<FilteringPredicate>("\"TAG tag1 AND FILTER filter1 OR TAG tag2 OR FILTER filter2\"")
            val alternatePredicate =
                json.decodeFromString<FilteringPredicate>("\"TAG tag3 AND FILTER filter4 OR TAG tag5\"")

            defaultFilteringInfo = FilteringInfo(
                predicate, mapOf(
                    "filter1" to FilterInfo.CheckBox(
                        "checkBox1", FilteringPredicate.Operator.OR, listOf(
                            FilterInfo.CheckBox.Option(
                                "option11",
                                FilteringPredicate.Tag("option11tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option12",
                                FilteringPredicate.Tag("option12tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option13",
                                FilteringPredicate.Tag("option13tag")
                            )
                        ),
                        index = 1
                    ),
                    "filter2" to FilterInfo.CheckBox(
                        "checkBox2", FilteringPredicate.Operator.AND, listOf(
                            FilterInfo.CheckBox.Option(
                                "option21",
                                FilteringPredicate.Tag("option21tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option22",
                                FilteringPredicate.Tag("option22tag")
                            )
                        ),
                        index = 2
                    )
                )
            )
            secondFilteringInfo = FilteringInfo(
                alternatePredicate, mapOf(
                    "filter4" to FilterInfo.CheckBox(
                        "checkBox4", FilteringPredicate.Operator.OR, listOf(
                            FilterInfo.CheckBox.Option(
                                "option41",
                                FilteringPredicate.Tag("option41tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option42",
                                FilteringPredicate.Tag("option42tag")
                            ),
                            FilterInfo.CheckBox.Option(
                                "option43",
                                FilteringPredicate.Tag("option43tag")
                            )
                        ),
                        index = 1
                    ),
                )
            )
            handler = ConcreteFilteringHandler(
                filteringInfoMap = mapOf(
                    DEFAULT to defaultFilteringInfo,
                    MY_FAVORITES to secondFilteringInfo
                )
            )
            handler.switchMode(MY_FAVORITES)
        }

        @Test
        fun currentStates_shouldReturnAllStates() {
            val infoMap = handler.currentStatesToInfoMap

            infoMap.size shouldBe 2

            with(infoMap[DEFAULT].assertNotNull()) {
                predicate shouldBe defaultFilteringInfo.predicate
                filters shouldBe defaultFilteringInfo.filters
            }

            with(infoMap[MY_FAVORITES].assertNotNull()) {
                predicate shouldBe secondFilteringInfo.predicate
                filters shouldBe secondFilteringInfo.filters
            }
        }

        @Test
        fun getQuery_withUnchangedState_shouldBeValid() {
            runTest {
                assertThat(
                    handler.predicate.first()?.toSQL()
                ).isEqualTo("(tags LIKE '%\"tag3\"%' OR tags LIKE '%\"tag5\"%')")
            }
        }

        @Test
        fun selectorData_shouldReflectState() {
            //given
            val filters = handler.buildSelectorData().filters
            val filterGroup = filters["filter4"]
            val title = filterGroup?.get(0) as FilterSelectorData.Cell.Title
            val cell = filterGroup[1] as FilterSelectorData.Cell.Option

            //then
            assertThat(filters.size).isEqualTo(1)
            assertThat(filterGroup.size).isEqualTo(4)
            assertThat(title.title).isEqualTo("checkBox4")
            assertThat(cell.label).isEqualTo("option41")
            assertThat(cell.isActive).isFalse
        }

        @Test
        fun tapOnCell_shouldUpdateState() {
            //when
            clickOnCell(1)
            clickOnCell(2)

            //then
            runTest {
                assertThat(handler.predicate.first()?.toSQL()).isEqualTo(
                    "(" +
                            "(tags LIKE '%\"tag3\"%' AND " +
                            "(tags LIKE '%\"option41tag\"%' OR tags LIKE '%\"option42tag\"%')) OR " +
                            "tags LIKE '%\"tag5\"%')"
                )
            }
            assertThat(
                (handler.currentStatesToInfoMap[MY_FAVORITES]?.filters?.get("filter4") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isTrue
            assertThat(
                (handler.currentStatesToInfoMap[MY_FAVORITES]?.filters?.get("filter4") as? FilterInfo.CheckBox)?.options?.get(
                    1
                )?.isActive
            ).isTrue
        }

        @Test
        fun barData_shouldReflectState() {
            //given
            val barData = handler.buildBarData(mockk(), "")
            val filteringBarCell = barData.filters[0]

            //then
            assertThat(barData.filters.size).isEqualTo(1)
            assertThat(filteringBarCell.isSelected).isFalse
            assertThat(filteringBarCell.name).isEqualTo("checkBox4")
        }

        @Test
        fun barData_cellName_shouldAdapt_toActiveFilters() {
            clickOnCell(1)
            assertThat(
                handler.buildBarData(
                    mockk(),
                    ""
                ).filters[0].name
            ).isEqualTo("option41")

            clickOnCell(2)
            assertThat(
                handler.buildBarData(
                    mockk(),
                    ""
                ).filters[0].name
            ).isEqualTo("option41, option42")

            clickOnCell(3)
            assertThat(
                handler.buildBarData(
                    mockk(),
                    ""
                ).filters[0].name
            ).isEqualTo("checkBox4 (3)")
        }

        @Test
        fun clear_shouldResetFilters() {
            assertThat(
                (handler.currentStatesToInfoMap[MY_FAVORITES]?.filters?.get("filter4") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isFalse
            clickOnCell(1)
            assertThat(
                (handler.currentStatesToInfoMap[MY_FAVORITES]?.filters?.get("filter4") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isTrue
            handler.buildSelectorData().onClearTap(listOf("filter4"))
            assertThat(
                (handler.currentStatesToInfoMap[MY_FAVORITES]?.filters?.get("filter4") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isFalse
        }

        @Test
        fun updateShouldDoNothing() {
            handler.update(FilteringState.Update("filter4", FilteringState.Update.Action.Clear))
            assertThat(
                (handler.currentStatesToInfoMap[MY_FAVORITES]?.filters?.get("filter4") as? FilterInfo.CheckBox)?.options?.get(
                    0
                )?.isActive
            ).isFalse
        }

        @Test
        fun switchModeShouldSucceed() {
            assertThat(handler.currentMode).isEqualTo(MY_FAVORITES)
            handler.switchMode(mode = DEFAULT)
            assertThat(handler.currentMode).isEqualTo(DEFAULT)
        }

        private fun clickOnCell(index: Int) {
            (handler.buildSelectorData().filters["filter4"]?.get(index) as FilterSelectorData.Cell.Option).onTap()
        }
    }

    @Nested
    inner class WithoutFilters {

        private lateinit var handler: ConcreteFilteringHandler

        @BeforeEach
        internal fun setUp() {
            handler = ConcreteFilteringHandler(filteringInfoMap = emptyMap())
        }

        @Test
        fun getQuery_withUnchangedState_shouldBeValid() {
            runTest {
                assertThat(handler.predicate.first()?.toSQL()).isNull()
            }
        }

        @Test
        fun selectorData_shouldReflectState() {
            //given
            val filters = handler.buildSelectorData().filters

            //then
            assertThat(filters.size).isEqualTo(0)
        }

        @Test
        fun updateShouldDoNothing() {
            handler.update(FilteringState.Update("filter1", FilteringState.Update.Action.Clear))
            runTest {
                assertThat(handler.predicate.first()).isNull()
            }
            assertThat(handler.selectorDataState.value.filters).isEmpty()
        }

        @Test
        fun switchModeShouldSucceed() {
            assertThat(handler.currentMode).isEqualTo(DEFAULT)
            handler.switchMode(mode = DEFAULT)
            assertThat(handler.currentMode).isEqualTo(DEFAULT)
        }
    }
}
