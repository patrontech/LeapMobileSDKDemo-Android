package com.greencopper.maps.geomap.searchProvider

import android.net.Uri
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.maps.common.LocationData
import com.greencopper.maps.recipe.LocationDetailConfigurationData
import com.greencopper.maps.searchProvider.LocationsSearchProvider
import com.greencopper.mapsmocks.MockMapsRepository
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.function.Predicate

internal class LocationsSearchProviderTest {
    private lateinit var searchProvider: LocationsSearchProvider
    private lateinit var mockMapsRepo: MockMapsRepository
    private val routeLink = "app://routeLink"

    private val id1 = "1"
    private val data1 = LocationDetailConfigurationData(
        "loc1", "sub1", "add1", emptyList(), "des1", null, listOf("tag11", "tag12")
    )

    private val locations = listOf(
        LocationData(
            id1,
            "loc1",
            "sub1",
            "add1",
            emptyList(),
            "des1",
            null,
            listOf("tag11", "tag12"),
        ),
        LocationData(
            "2",
            "loc2",
            "sub2",
            "add2",
            emptyList(),
            "des2",
            null,
            listOf("tag21", "tag22"),
        ),
        LocationData(
            "3",
            "loc3",
            "sub3",
            "add3",
            emptyList(),
            "des3",
            null,
            listOf("tag31", "tag32"),
        ),
    )

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        val paramSlot = slot<String>()
        val idSlot = slot<String>()
        mockkStatic(Uri::class)
        val uri = mockk<Uri>()
        val builder = mockk<Uri.Builder>()
        every { Uri.parse(any()) } returns uri
        every { uri.buildUpon() } returns builder
        every { builder.appendQueryParameter(capture(paramSlot), capture(idSlot)) } returns builder
        every { builder.build() } returns uri
        every { uri.toString() } answers { "$routeLink?${paramSlot.captured}=${idSlot.captured}" }

        mockMapsRepo = MockMapsRepository(locations, locations)
        searchProvider = LocationsSearchProvider(
            mockMapsRepo,
            MockLocalizationService()
        )
    }

    @Test
    fun test_serialization() {
        val params = LocationsSearchProvider.Params(
            FilteringPredicate.Tag("tag1"),
            listOf("name", "subtitle", "tags", "description", "address"),
            routeLink,
        )

        testKiboSerializable(params)

        val params2 = LocationsSearchProvider.Params(
            null,
            listOf("name", "subtitle", "tags", "description", "address"),
            routeLink,
        )
        val deserializedParams = searchProvider.deserialize(params2.encodeToJsonElement())
        assertThat(params2).isEqualTo(deserializedParams)
    }

    @Test
    fun query_withNullPredicate_shouldReturnAll() {
        //given
        val params = LocationsSearchProvider.Params(
            null,
            listOf("name", "subtitle", "tags", "description", "address"),
            routeLink,
        )

        //when
        mockMapsRepo.rearrangedLocations = rearrangeLocations(params.predicate?.query()?.toPredicate())
        val entries = searchProvider.entries(params)

        //then
        assertThat(entries.size).isEqualTo(3)
    }

    @Test
    fun query_withPredicate_shouldReturnFilteredEntries() {
        //given
        mockMapsRepo.rearrangedLocationsWithPredicate = locations.minus(locations[1])
        val predicate = FilteringPredicate.Logic(
            FilteringPredicate.Tag("tag11"),
            FilteringPredicate.Operator.OR,
            FilteringPredicate.Tag("tag21"),
        )
        val params = LocationsSearchProvider.Params(
            predicate,
            listOf("name", "subtitle", "tags", "description", "address"),
            "routeLink",
        )

        //when
        mockMapsRepo.rearrangedLocations = rearrangeLocations(params.predicate?.query()?.toPredicate())
        val entries = searchProvider.entries(params)

        //then
        assertThat(entries.size).isEqualTo(2)
        val entry = entries.find { it.matches.contains(data1.name) }
        assertThat(entry).isNotNull
        assertThat(entry?.viewData?.routeLink).isEqualTo("$routeLink?locationId=\"${id1}\"")
    }

    @Test
    fun query_withMissingFields_shouldNotReturnThoseFields() {
        val params = LocationsSearchProvider.Params(
            null,
            listOf(),
            "routeLink",
        )

        val entries = searchProvider.entries(params)

        assertThat(entries.size).isEqualTo(locations.size)
        assertThat(entries[0].matches).isEmpty()
    }

    private fun rearrangeLocations(tagsFilter: Predicate<List<String>>? = null, sortedByName: Boolean = false):
            List<LocationData> {
        val filteredLocations = locations
            .filter {
                tagsFilter?.test(it.tags) ?: true
            }.toList()
        return if (sortedByName) {
            filteredLocations.sortedBy { entry ->
                entry.name
            }
        } else {
            filteredLocations
        }
    }

    @Test
    fun query_withNullFields_shouldReturnDefaultFields() {
        val params = LocationsSearchProvider.Params(
            null,
            routeLink = "routeLink",
        )

        val entries = searchProvider.entries(params)

        assertThat(entries.size).isEqualTo(locations.size)
        assertThat(entries[0].matches).hasSize(2)
        assertThat(entries[0].matches).contains("loc1")
        assertThat(entries[0].matches).contains("sub1")
    }
}
