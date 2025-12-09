package com.greencopper.event.searchProvider

import com.greencopper.event.performers.Performer
import com.greencopper.eventmocks.MockPerformerRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PerformersSearchProviderTest {
    private lateinit var searchProvider: PerformersSearchProvider
    private lateinit var mockPerformersRepo: MockPerformerRepository
    private val routeLink = "app://routeLink"

    private val performers = listOf(
        Performer(
            itemId = "1",
            name = "perf1",
            subtitle = "sub1",
            description = "desc1",
            photos = listOf(),
            tags = listOf("tag11", "tag12")
        ),
        Performer(
            itemId = "2",
            name = "perf2",
            subtitle = "sub2",
            description = "desc2",
            photos = listOf(),
            tags = listOf("tag21", "tag22")
        ),
        Performer(
            itemId = "3",
            name = "perf3",
            subtitle = "sub3",
            description = "desc3",
            photos = listOf(),
            tags = listOf("tag31", "tag32")
        ),
    )

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        mockPerformersRepo = MockPerformerRepository(performers)
        searchProvider = PerformersSearchProvider(
            mockPerformersRepo,
            MockLocalizationService(),
            MockLinkResolver(),
        )
    }

    @Test
    fun test_serialization() {
        val params = PerformersSearchProvider.Params(
            FilteringPredicate.Tag("tag1"),
            listOf("name", "subtitle", "tags", "description"),
            routeLink
        )

        testKiboSerializable(params)

        val params2 = PerformersSearchProvider.Params(
            null,
            listOf("name", "subtitle", "tags", "description"),
            routeLink
        )
        val deserializedParams = searchProvider.deserialize(params2.encodeToJsonElement())
        assertThat(params2).isEqualTo(deserializedParams)
    }

    @Test
    fun query_withNullPredicate_shouldReturnAll() {
        //given
        val params = PerformersSearchProvider.Params(
            null,
            listOf("name", "subtitle", "tags", "description"),
            routeLink
        )

        //when
        val entries = searchProvider.entries(params)

        //then
        assertThat(entries.size).isEqualTo(3)
    }

    @Test
    fun query_withPredicate_shouldReturnFilteredEntries() {
        //given
        mockPerformersRepo.performersWithPredicate = performers.minus(performers[1])
        val predicate = FilteringPredicate.Logic(
            FilteringPredicate.Tag("tag11"),
            FilteringPredicate.Operator.OR,
            FilteringPredicate.Tag("tag21")
        )
        val params = PerformersSearchProvider.Params(
            predicate,
            listOf("name", "subtitle", "tags", "description"),
            "routeLink"
        )

        //when
        val entries = searchProvider.entries(params)

        //then
        assertThat(entries.size).isEqualTo(2)
        assertThat(entries.first().matches).contains(performers.first().name)
    }

    @Test
    fun query_withMissingFields_shouldNotReturnThoseFields() {
        val params = PerformersSearchProvider.Params(
            null,
            listOf(),
            "routeLink",
        )

        val entries = searchProvider.entries(params)

        assertThat(entries.size).isEqualTo(performers.size)
        assertThat(entries[0].matches).isEmpty()
    }

    @Test
    fun query_withNullFields_shouldReturnDefaultFields() {
        val params = PerformersSearchProvider.Params(
            null,
            routeLink = "routeLink",
        )

        val entries = searchProvider.entries(params)

        assertThat(entries.size).isEqualTo(performers.size)
        assertThat(entries[0].matches).hasSize(2)
        assertThat(entries[0].matches).contains("perf1")
        assertThat(entries[0].matches).contains("sub1")
    }
}
