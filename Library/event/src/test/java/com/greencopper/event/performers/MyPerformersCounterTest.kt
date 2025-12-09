package com.greencopper.event.performers

import com.greencopper.eventmocks.MockPerformerRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class MyPerformersCounterTest {
    private lateinit var classUnderTest: MyPerformersCounter
    private lateinit var mockPerformerRepository: MockPerformerRepository
    private lateinit var mockMyPerformersManager: MockFavoritesManager<String>
    private val mockLogger: MockLogging = MockLogging()

    private val performers = listOf(
        Performer(
            "1",
            "name",
            "subtitle",
            "description",
            photos = listOf("photo"),
            tags = listOf("tag"),
        ),
        Performer(
            "2",
            "name",
            "subtitle",
            "description",
            photos = listOf("photo"),
            tags = listOf("tag2"),
        ),
        Performer(
            "3",
            "A-name",
            "subtitle",
            "description",
            photos = listOf("photo"),
            tags = listOf("tag"),
        ),
    )

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
    }

    @Nested
    @DisplayName("Given my performers is empty")
    inner class EmptyMyPerformers {
        private val counterParams = MyPerformersCounterParams()
        private val counterParamsWithPredicate = MyPerformersCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockPerformerRepository = MockPerformerRepository(performers)
            mockMyPerformersManager = MockFavoritesManager()
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 0")
        fun countWithoutPredicateShouldReturnZero() {
            classUnderTest = MyPerformersCounter(
                params = counterParams.encodeToJsonElement(),
                myPerformersManager = mockMyPerformersManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 0")
        fun countWithStaticPredicateShouldReturnZero() {
            classUnderTest = MyPerformersCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myPerformersManager = mockMyPerformersManager,
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

            classUnderTest = MyPerformersCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myPerformersManager = mockMyPerformersManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 0")
        fun countWithInvalidParamsShouldReturnZero() {
            classUnderTest = MyPerformersCounter(
                params = JsonNull,
                myPerformersManager = mockMyPerformersManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }
    }

    @Nested
    @DisplayName("Given 3 items are in my performers")
    inner class NotEmptyMyPerformers {
        private val counterParams = MyPerformersCounterParams()
        private val counterParamsWithPredicate = MyPerformersCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockPerformerRepository = MockPerformerRepository(performers)
            mockMyPerformersManager = MockFavoritesManager(performers.map { it.itemId }.toMutableSet())
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 3")
        fun countWithoutPredicateShouldReturnThree() {
            mockMyPerformersManager.favoriteIds = performers.map { it.itemId }.toMutableSet()
            mockMyPerformersManager.repository = MockPerformerRepository(performers = performers)
            classUnderTest = MyPerformersCounter(
                params = counterParams.encodeToJsonElement(),
                myPerformersManager = mockMyPerformersManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 2")
        fun countWithStaticPredicateShouldReturnTwo() {
            mockMyPerformersManager.favoriteIds = performers.map { it.itemId }.toMutableSet()
            mockMyPerformersManager.repository =
                MockPerformerRepository(performersWithPredicate = performers.minus(performers[1]))
            classUnderTest = MyPerformersCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myPerformersManager = mockMyPerformersManager,
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

            mockMyPerformersManager.favoriteIds = performers.map { it.itemId }.toMutableSet()
            mockMyPerformersManager.repository = MockPerformerRepository(performersWithPredicate = performers)
            classUnderTest = MyPerformersCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myPerformersManager = mockMyPerformersManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 3")
        fun countWithInvalidParamsShouldReturnThree() {
            mockMyPerformersManager.favoriteIds = performers.map { it.itemId }.toMutableSet()
            mockMyPerformersManager.repository = MockPerformerRepository(performers = performers)
            classUnderTest = MyPerformersCounter(
                params = JsonNull,
                myPerformersManager = mockMyPerformersManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }
    }
}
