package com.greencopper.event.performers

import com.greencopper.eventmocks.MockPerformerRepository
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.testmocks.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class PerformersListProviderTest {
    private val provider: ListProvider

    private val performers = listOf(
        Performer(
            itemId = "0",
            name = "name0",
            subtitle = "subtitle0",
            description = "description0",
            photos = listOf("photo01", "photo02"),
            tags = listOf("tag01", "tag02"),
            order = 4,
        ),
        Performer(
            itemId = "1",
            name = "name1",
            subtitle = "subtitle1",
            description = "description1",
            photos = emptyList(),
            tags = listOf("tag11", "tag12"),
            order = 1,
        ),
    )

    init {
        val performersRepo = MockPerformerRepository(
            performers
        )

        provider = PerformersListProvider(performersRepo)
    }

    @Test
    fun provider_shouldGetList() = runTest {
        val result = provider.getElements()

        result shouldBe performers.map { it.toListProviderElement() }
    }

    private fun Performer.toListProviderElement(): ListProvider.Element = ListProvider.Element(
        id = itemId,
        order = order,
        title = name,
        subtitle = subtitle,
        tags = tags,
        image = photos.firstOrNull()
    )
}
