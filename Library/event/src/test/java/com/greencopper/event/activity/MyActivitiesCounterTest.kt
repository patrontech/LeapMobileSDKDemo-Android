package com.greencopper.event.activity

import com.greencopper.eventmocks.MockActivityRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class MyActivitiesCounterTest {
    private lateinit var classUnderTest: MyActivitiesCounter
    private lateinit var mockActivitiesRepository: MockActivityRepository
    private lateinit var mockMyActivitiesManager: MockFavoritesManager<Long>
    private val mockLogger: MockLogging = MockLogging()

    private val activities = listOf(
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
        Toolkit.setupTest()
    }

    @Nested
    @DisplayName("Given my activities is empty")
    inner class EmptyMyActivities {
        private val counterParams = MyActivitiesCounterParams()
        private val counterParamsWithPredicate = MyActivitiesCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockActivitiesRepository = MockActivityRepository(activities)
            mockMyActivitiesManager = MockFavoritesManager()
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 0")
        fun countWithoutPredicateShouldReturnZero() {
            classUnderTest = MyActivitiesCounter(
                params = counterParams.encodeToJsonElement(),
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 0")
        fun countWithStaticPredicateShouldReturnZero() {
            classUnderTest = MyActivitiesCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

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

            classUnderTest = MyActivitiesCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 0")
        fun countWithInvalidParamsShouldReturnZero() {
            classUnderTest = MyActivitiesCounter(
                params = JsonNull,
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }
    }

    @Nested
    @DisplayName("Given 3 items are in my activities")
    inner class NotEmptyMyActivities {
        private val counterParams = MyActivitiesCounterParams()
        private val counterParamsWithPredicate = MyActivitiesCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockActivitiesRepository = MockActivityRepository(activities)
            mockMyActivitiesManager = MockFavoritesManager(activities.map { it.itemId }.toMutableSet())
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 3")
        fun countWithoutPredicateShouldReturnThree() {
            mockMyActivitiesManager.favoriteIds = activities.map { it.itemId }.toMutableSet()
            mockMyActivitiesManager.repository = MockActivityRepository(activities = activities)
            classUnderTest = MyActivitiesCounter(
                params = counterParams.encodeToJsonElement(),
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 2")
        fun countWithStaticPredicateShouldReturnTwo() {
            mockMyActivitiesManager.favoriteIds = activities.map { it.itemId }.toMutableSet()
            mockMyActivitiesManager.repository =
                MockActivityRepository(activitiesWithPredicate = activities.minus(activities[1]))
            classUnderTest = MyActivitiesCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

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

            mockMyActivitiesManager.favoriteIds = activities.map { it.itemId }.toMutableSet()
            mockMyActivitiesManager.repository = MockActivityRepository(activitiesWithPredicate = activities)
            classUnderTest = MyActivitiesCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 3")
        fun countWithInvalidParamsShouldReturnThree() {
            mockMyActivitiesManager.favoriteIds = activities.map { it.itemId }.toMutableSet()
            mockMyActivitiesManager.repository = MockActivityRepository(activities = activities)
            classUnderTest = MyActivitiesCounter(
                params = JsonNull,
                myActivitiesManager = mockMyActivitiesManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }
    }
}
