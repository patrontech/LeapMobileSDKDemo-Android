package com.greencopper.event.scheduleItem

import com.greencopper.eventmocks.MockScheduleItemRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class MyScheduleCounterTest {
    private lateinit var classUnderTest: MyScheduleCounter
    private lateinit var mockScheduleItemRepository: MockScheduleItemRepository
    private lateinit var mockMyScheduleManager: MockFavoritesManager<Long>
    private val mockLogger: MockLogging = MockLogging()

    private val scheduleItems = listOf(
        ScheduleItem(
            1,
            12,
            45,
            "name",
            photos = listOf("photo"),
            tags = listOf("tag"),
        ),
        ScheduleItem(
            2,
            23,
            56,
            "name2",
            photos = listOf("photo"),
            tags = listOf("tag2"),
        ),
        ScheduleItem(
            3,
            12,
            45,
            "A-name",
            photos = listOf("photo"),
            tags = listOf("tag"),
        ),
    )

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
    }

    @Nested
    @DisplayName("Given my schedule is empty")
    inner class EmptyMySchedule {
        private val counterParams = MyScheduleCounterParams()
        private val counterParamsWithPredicate = MyScheduleCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockScheduleItemRepository = MockScheduleItemRepository(scheduleItems)
            mockMyScheduleManager = MockFavoritesManager()
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 0")
        fun countWithoutPredicateShouldReturnZero() {
            classUnderTest = MyScheduleCounter(
                params = counterParams.encodeToJsonElement(),
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 0")
        fun countWithStaticPredicateShouldReturnZero() {
            classUnderTest = MyScheduleCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )
            counterParamsWithPredicate.predicate?.let { mockScheduleItemRepository.setupQueryForTags(it) }

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with a dynamic predicate, Then it should return 0")
        fun countWithDynamicPredicateShouldReturnZero() {
            val dynamicPredicate = FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag2")
            )

            classUnderTest = MyScheduleCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )
            mockScheduleItemRepository.setupQueryForTags(dynamicPredicate)

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 0")
        fun countWithInvalidParamsShouldReturnZero() {
            classUnderTest = MyScheduleCounter(
                params = JsonNull,
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }
    }

    @Nested
    @DisplayName("Given 3 items are in my schedule")
    inner class NotEmptyMySchedule {
        private val counterParams = MyScheduleCounterParams()
        private val counterParamsWithPredicate = MyScheduleCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockScheduleItemRepository = MockScheduleItemRepository(scheduleItems)
            mockMyScheduleManager = MockFavoritesManager(scheduleItems.map { it.itemId }.toMutableSet())
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 3")
        fun countWithoutPredicateShouldReturnThree() {
            classUnderTest = MyScheduleCounter(
                params = counterParams.encodeToJsonElement(),
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 2")
        fun countWithStaticPredicateShouldReturnTwo() {
            classUnderTest = MyScheduleCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )
            counterParamsWithPredicate.predicate?.let { mockScheduleItemRepository.setupQueryForTags(it) }

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(2)
            }
        }

        @Test
        @DisplayName("When count is called with a dynamic predicate, Then it should return 3")
        fun countWithDynamicPredicateShouldReturnThree() {
            val dynamicPredicate = FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag2")
            )

            classUnderTest = MyScheduleCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )
            mockScheduleItemRepository.setupQueryForTags(dynamicPredicate)

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 3")
        fun countWithInvalidParamsShouldReturnThree() {
            classUnderTest = MyScheduleCounter(
                params = JsonNull,
                scheduleItemRepository = mockScheduleItemRepository,
                myScheduleManager = mockMyScheduleManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }
    }
}
