package com.greencopper.maps.recipe

import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.maps.common.LocationData
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ConcreteMapsRepositoryTest {
    init {
        Toolkit.setupTest()
    }

    private val localizationService = MockLocalizationService()
    private val repository: MapsRepository = ConcreteMapsRepository(localizationService)
    private val mapConfigurationWithoutOrder: MapsConfiguration = MapsConfiguration(
        locations = mapOf(
            "3" to LocationDetailConfigurationData("3", tags = listOf("tag31", "tag32")),
            "2" to LocationDetailConfigurationData("2", tags = listOf("tag21", "tag22")),
            "1" to LocationDetailConfigurationData("1", tags = listOf("tag11", "tag12"))
        )
    )
    private val mapConfigurationWithOrder: MapsConfiguration = MapsConfiguration(
        locations = mapOf(
            "3" to LocationDetailConfigurationData("3", tags = listOf("tag31", "tag32"), order = 1),
            "2" to LocationDetailConfigurationData("2", tags = listOf("tag21", "tag22"), order = null),
            "1" to LocationDetailConfigurationData("1", tags = listOf("tag11", "tag12"), order = 3)
        )
    )

    @Test
    fun getLocationsTest() {
        assertThat(repository.getLocations()).isEqualTo(listOf<LocationData>())
        repository.setConfiguration(mapConfigurationWithoutOrder)
        val expectedResult = mapConfigurationWithoutOrder.locations.entries.map { entry ->
            LocationData(
                itemId = entry.key,
                name = entry.value.name,
                subtitle = entry.value.subtitle,
                address = entry.value.address,
                images = emptyList(),
                description = entry.value.description,
                bottomWidgetCollection = entry.value.bottomWidgetCollection,
                tags = entry.value.tags ?: emptyList(),
                order = entry.value.order,
            )
        }
        assertThat(repository.getLocations()).isEqualTo(expectedResult)
    }

    @Test
    fun getLocationByIdTest() {
        repository.setConfiguration(mapConfigurationWithoutOrder)
        val expectedResult = mapConfigurationWithoutOrder.locations["1"]?.let {
            LocationData(
                itemId = "1",
                name = it.name,
                subtitle = it.subtitle,
                address = it.address,
                images = emptyList(),
                description = it.description,
                bottomWidgetCollection = it.bottomWidgetCollection,
                tags = listOf("tag11", "tag12"),
                order = it.order,
            )
        }
        assertThat(repository.getLocation("1")).isEqualTo(expectedResult)
    }

    @Test
    fun getLocationsSortedByNameWithoutOrderTest() {
        repository.setConfiguration(mapConfigurationWithoutOrder)
        val locationsSortedByName = repository.getLocationsRearranged(null, true)
        val iterator = locationsSortedByName.iterator()
        val first = iterator.next()
        val second = iterator.next()
        assertThat(first.name).isEqualTo("1")
        assertThat(second.name).isEqualTo("2")
    }

    @Test
    fun getLocationsSortedByNameWithOrderTest() {
        repository.setConfiguration(mapConfigurationWithOrder)
        val locationsSortedByName = repository.getLocationsRearranged(null, true)
        val iterator = locationsSortedByName.iterator()
        val first = iterator.next()
        val second = iterator.next()
        assertThat(first.name).isEqualTo("3")
        assertThat(second.name).isEqualTo("1")
    }

    @Test
    fun getLocationsFilteredTest() {
        repository.setConfiguration(mapConfigurationWithoutOrder)
        val predicate = FilteringPredicate.Logic(
            FilteringPredicate.Tag("tag11"),
            FilteringPredicate.Operator.OR,
            FilteringPredicate.Tag("tag21")
        )
        val locationsFiltered = repository.getLocationsRearranged(predicate.query()?.toPredicate())
        assertThat(locationsFiltered.map { it.itemId }).contains("1")
        assertThat(locationsFiltered.map { it.itemId }).contains("2")
    }

    @Test
    @DisplayName("Given item are found in database, When calling getListData without a predicate, Then these items are returned")
    fun getListDataWithoutPredicateShouldSucceed() {
        repository.setConfiguration(mapConfigurationWithoutOrder)
        runTest {
            val result = repository.getListData().first()
            assertThat(result).hasSize(3)
            assertThat(result[0].itemId).isEqualTo("1")
            assertThat(result[1].itemId).isEqualTo("2")
            assertThat(result[2].itemId).isEqualTo("3")
        }
    }

    @Test
    @DisplayName("Given items are found in database, When calling getListData with a predicate, Then these items are returned")
    fun getListDataWithPredicateShouldSucceed() {
        repository.setConfiguration(mapConfigurationWithoutOrder)
        runTest {
            val predicate = FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag11"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Tag("tag21")
            )
            val items = repository.getListData(predicate).first()

            assertThat(items).hasSize(2)
            assertThat(items[0].itemId).isEqualTo("1")
            assertThat(items[1].itemId).isEqualTo("2")
        }
    }
}
