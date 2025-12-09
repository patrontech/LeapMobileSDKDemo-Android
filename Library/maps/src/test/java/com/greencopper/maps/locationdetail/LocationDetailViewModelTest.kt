package com.greencopper.maps.locationdetail

import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.maps.common.LocationData
import com.greencopper.mapsmocks.MockMapsRepository
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.interfacekit.MockWidgetResolver
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LocationDetailViewModelTest {

    private val mapsRepo = MockMapsRepository()
    private val favoritesManager = MockFavoritesManager<String>()

    private val viewModel = LocationDetailViewModel(
        MockLocalizationService(),
        mapsRepo,
        MockWidgetResolver(),
        favoritesManager
    )

    @BeforeEach
    fun setUp() {
        mapsRepo.mockLocations = listOf(
            LocationData(
                itemId = "1",
                name = "Gotham",
            ),
            LocationData(
                itemId = "1",
                name = "Metropolis",
            ),
        )
    }

    @Test
    fun repoHasLocationDetails_getLocationDetails_returnsLocation() {
        runBlocking {
            assertThat(viewModel.getLocationDetails("1", listOf(DisplayableTag("tag"))).firstOrNull()?.name).isEqualTo("Gotham")
        }
    }

    @Test
    fun repoHasWrongDetails_getLocationDetails_returnsNull() {
        runBlocking {
            assertThat(viewModel.getLocationDetails("3", listOf(DisplayableTag("tag"))).firstOrNull()).isNull()
        }
    }

    @Test
    fun getWidgetItems_returnsSameNumberWidgetItems() {
        val info = listOf(
            WidgetCollectionConfiguration.Instance.WidgetInfo(
                WidgetCollectionConfiguration.Instance.WidgetKey("", 1),
                JsonNull,
            ),
            WidgetCollectionConfiguration.Instance.WidgetInfo(
                WidgetCollectionConfiguration.Instance.WidgetKey("", 2),
                JsonNull,
            )
        )
        val result = viewModel.getWidgetItems(info)
        assertThat(result.size).isEqualTo(2)

        val result2 = viewModel.getWidgetItems(info)
        assertThat(result === result2).isTrue
    }
}
