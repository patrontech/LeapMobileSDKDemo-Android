package com.greencopper.event.activity

import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.interfacekit.list.provider.ListProvider
import kotlinx.coroutines.flow.first

internal class ActivitiesListProvider(
    private val activityRepository: ActivityRepository,
) : ListProvider {

    companion object {
        const val key = "ListProvider.Activities"
    }

    override suspend fun getElements(): List<ListProvider.Element> {
        return activityRepository.getActivities().first()
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
