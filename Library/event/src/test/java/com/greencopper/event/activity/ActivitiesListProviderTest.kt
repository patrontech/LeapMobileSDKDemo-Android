package com.greencopper.event.activity

import com.greencopper.eventmocks.MockActivityRepository
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.testmocks.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class ActivitiesListProviderTest {

    private val provider: ListProvider

    private val contentActivities = listOf(
        ContentActivity(
            itemId = 0,
            name = "name0",
            subtitle = "subtitle0",
            description = "description0",
            photos = listOf("photo01", "photo02"),
            tags = listOf("tag01", "tag02"),
            order = 4,
        ),
        ContentActivity(
            itemId = 1,
            name = "name1",
            subtitle = "subtitle1",
            description = "description1",
            photos = emptyList(),
            tags = listOf("tag11", "tag12"),
            order = 1,
        ),
    )

    init {
        val activitiesRepo = MockActivityRepository(
            contentActivities
        )

        provider = ActivitiesListProvider(activitiesRepo)
    }

    @Test
    fun provider_shouldGetList() = runTest {
        val result = provider.getElements()

        result shouldBe contentActivities.map { it.toListProviderElement() }
    }

    private fun ContentActivity.toListProviderElement(): ListProvider.Element = ListProvider.Element(
        id = itemId,
        order = order,
        title = name,
        subtitle = subtitle,
        tags = tags,
        image = photos.firstOrNull()
    )
}
