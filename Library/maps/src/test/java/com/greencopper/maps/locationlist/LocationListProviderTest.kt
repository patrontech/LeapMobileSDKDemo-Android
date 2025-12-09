package com.greencopper.maps.locationlist

import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.maps.common.LocationData
import com.greencopper.mapsmocks.MockMapsRepository
import com.greencopper.testmocks.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class LocationListProviderTest {

    private val provider: ListProvider

    private val locations = listOf(
        LocationData(
            itemId = "0",
            name = "name0",
            subtitle = "subtitle0",
            description = "description0",
            images = listOf("photo01", "photo02"),
            tags = listOf("tag01", "tag02"),
            order = 4,
        ),
        LocationData(
            itemId = "1",
            name = "name1",
            subtitle = "subtitle1",
            description = "description1",
            images = emptyList(),
            tags = listOf("tag11", "tag12"),
            order = 1,
        ),
    )

    init {
        val mapsRepo = MockMapsRepository(
            mockLocations = locations
        )

        provider = LocationsListProvider(mapsRepo)
    }

    @Test
    fun provider_shouldGetList() = runTest {
        val result = provider.getElements()

        result shouldBe locations.map { it.toListProviderElement() }
    }

    private fun LocationData.toListProviderElement(): ListProvider.Element = ListProvider.Element(
        id = itemId,
        order = order,
        title = name,
        subtitle = subtitle,
        tags = tags,
        image = images.firstOrNull()
    )
}
