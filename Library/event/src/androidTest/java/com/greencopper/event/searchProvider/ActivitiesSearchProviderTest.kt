package com.greencopper.event.searchProvider

import com.greencopper.event.activity.ContentActivity
import com.greencopper.eventmocks.MockActivityRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate.Logic
import com.greencopper.interfacekit.filtering.FilteringPredicate.Operator.OR
import com.greencopper.interfacekit.filtering.FilteringPredicate.Tag
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ActivitiesSearchProviderTest {

    private lateinit var searchProvider: ActivitiesSearchProvider
    private lateinit var mockActivityRepo: MockActivityRepository
    private val routeLink = "app://routeLink"

    private val activities = listOf(
        ContentActivity(
            itemId = 1,
            name = "act1",
            subtitle = "sub1",
            description = "desc1",
            photos = listOf(),
            tags = listOf("tag11", "tag12")
        ),
        ContentActivity(
            itemId = 2,
            name = "act2",
            subtitle = "sub2",
            description = "desc2",
            photos = listOf(),
            tags = listOf("tag21", "tag22")
        ),
        ContentActivity(
            itemId = 3,
            name = "act3",
            subtitle = "sub3",
            description = "desc3",
            photos = listOf(),
            tags = listOf("tag31", "tag32")
        ),
    )

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        mockActivityRepo = MockActivityRepository(activities)
        searchProvider = ActivitiesSearchProvider(
            mockActivityRepo,
            MockLocalizationService(),
            MockLinkResolver(),
        )
    }

    @Test
    fun test_serialization() {
        val params = ActivitiesSearchProvider.Params(
            Tag("tag1"),
            listOf("name", "subtitle", "tags", "description"),
            routeLink,
        )

        testKiboSerializable(params)

        val params2 = ActivitiesSearchProvider.Params(
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
        val params = ActivitiesSearchProvider.Params(
            null,
            listOf("name", "subtitle", "tags", "description"),
            routeLink,
        )

        //when
        val entries = searchProvider.entries(params)

        //then
        assertThat(entries.size).isEqualTo(3)
    }

    @Test
    fun query_withPredicate_shouldReturnFilteredEntries() {
        //given
        mockActivityRepo.activitiesWithPredicate = activities.minus(activities[1])
        val predicate = Logic(Tag("tag11"), OR, Tag("tag21"))
        val params = ActivitiesSearchProvider.Params(
            predicate,
            listOf("name", "subtitle", "tags", "description"),
            "routeLink",
        )

        //when
        val entries = searchProvider.entries(params)

        //then
        assertThat(entries.size).isEqualTo(2)
        assertThat(entries.first().matches).contains(activities.first().name)
    }

    @Test
    fun query_withMissingFields_shouldNotReturnThoseFields() {
        val params = ActivitiesSearchProvider.Params(
            null,
            listOf(),
            "routeLink",
        )

        val entries = searchProvider.entries(params)

        assertThat(entries.size).isEqualTo(activities.size)
        assertThat(entries[0].matches).isEmpty()
    }

    @Test
    fun query_withNullFields_shouldReturnDefaultFields() {
        val params = ActivitiesSearchProvider.Params(
            null,
            routeLink = "routeLink",
        )

        val entries = searchProvider.entries(params)

        assertThat(entries.size).isEqualTo(activities.size)
        assertThat(entries[0].matches).hasSize(2)
        assertThat(entries[0].matches).contains("act1")
        assertThat(entries[0].matches).contains("sub1")
    }
}
