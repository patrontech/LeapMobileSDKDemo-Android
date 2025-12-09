package com.greencopper.maps.common

import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.mapsmocks.MockMapsRepository
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class MyLocationsCounterTest {
    private lateinit var classUnderTest: MyLocationsCounter
    private lateinit var mockMyLocationsManager: MockFavoritesManager<String>
    private val mockLogger: MockLogging = MockLogging()

    private val locations = listOf(
        LocationData(
            itemId = "1",
            "first_location",
            "name_1",
            "address1",
            listOf("photo"),
            tags = listOf("tag"),
        ),
        LocationData(
            itemId = "2",
            "second_location",
            "name_2",
            "address2",
            listOf("photo"),
            tags = listOf("tag2"),
        ),
        LocationData(
            itemId = "3",
            "third_location",
            "A-name",
            "address3",
            listOf("photo"),
            tags = listOf("tag"),
        ),
    )

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
    }

    @Nested
    @DisplayName("Given my locations is empty")
    inner class EmptyMyLocations {
        private val counterParams = MyLocationsCounterParams()
        private val counterParamsWithPredicate = MyLocationsCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockMyLocationsManager = MockFavoritesManager()
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 0")
        fun countWithoutPredicateShouldReturnZero() {
            classUnderTest = MyLocationsCounter(
                params = counterParams.encodeToJsonElement(),
                myLocationsManager = mockMyLocationsManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 0")
        fun countWithStaticPredicateShouldReturnZero() {
            classUnderTest = MyLocationsCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myLocationsManager = mockMyLocationsManager,
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

            classUnderTest = MyLocationsCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myLocationsManager = mockMyLocationsManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 0")
        fun countWithInvalidParamsShouldReturnZero() {
            classUnderTest = MyLocationsCounter(
                params = JsonNull,
                myLocationsManager = mockMyLocationsManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(0)
            }
        }
    }

    @Nested
    @DisplayName("Given 3 items are in my locations")
    inner class NotEmptyMyLocations {
        private val counterParams = MyLocationsCounterParams()
        private val counterParamsWithPredicate = MyLocationsCounterParams(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag3")
            )
        )

        @BeforeEach
        internal fun setUp() {
            mockMyLocationsManager = MockFavoritesManager(locations.map { it.itemId }.toMutableSet())
        }

        @Test
        @DisplayName("When count is called without a predicate, Then it should return 3")
        fun countWithoutPredicateShouldReturnThree() {
            mockMyLocationsManager.favoriteIds = locations.map { it.itemId }.toMutableSet()
            mockMyLocationsManager.repository =
                MockMapsRepository(rearrangedLocations = locations)
            classUnderTest = MyLocationsCounter(
                params = counterParams.encodeToJsonElement(),
                myLocationsManager = mockMyLocationsManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with a static predicate, Then it should return 2")
        fun countWithStaticPredicateShouldReturnTwo() {
            mockMyLocationsManager.favoriteIds = locations.map { it.itemId }.toMutableSet()
            mockMyLocationsManager.repository =
                MockMapsRepository(
                    rearrangedLocationsWithPredicate = locations.minus(
                        locations[1]
                    )
                )
            classUnderTest = MyLocationsCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myLocationsManager = mockMyLocationsManager,
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

            mockMyLocationsManager.favoriteIds = locations.map { it.itemId }.toMutableSet()
            mockMyLocationsManager.repository =
                MockMapsRepository(rearrangedLocationsWithPredicate = locations)
            classUnderTest = MyLocationsCounter(
                params = counterParamsWithPredicate.encodeToJsonElement(),
                myLocationsManager = mockMyLocationsManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count(dynamicPredicate)).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When count is called with invalid params, Then it should return 3")
        fun countWithInvalidParamsShouldReturnThree() {
            mockMyLocationsManager.favoriteIds = locations.map { it.itemId }.toMutableSet()
            mockMyLocationsManager.repository =
                MockMapsRepository(rearrangedLocations = locations)
            classUnderTest = MyLocationsCounter(
                params = JsonNull,
                myLocationsManager = mockMyLocationsManager,
                logger = mockLogger,
            )

            runTest {
                assertThat(classUnderTest.count()).isEqualTo(3)
            }
        }
    }
}
