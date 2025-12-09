package com.greencopper.event.performers

import com.greencopper.event.performers.data.repository.PerformerRepository
import com.greencopper.interfacekit.list.provider.ListProvider
import kotlinx.coroutines.flow.first

internal class PerformersListProvider(
    private val performerRepository: PerformerRepository,
) : ListProvider {

    companion object {
        const val key = "ListProvider.Performers"
    }

    override suspend fun getElements(): List<ListProvider.Element> {
        return performerRepository.getPerformers().first()
            .map {
                ListProvider.Element(
                    id = it.itemId,
                    order = it.order,
                    title = it.name,
                    subtitle = it.subtitle,
                    tags = it.tags,
                    image = it.photos.firstOrNull()
                )
            }
    }
}
