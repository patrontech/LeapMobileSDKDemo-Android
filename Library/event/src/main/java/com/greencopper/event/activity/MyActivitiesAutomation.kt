package com.greencopper.event.activity

import com.greencopper.core.automation.AutomationKey
import com.greencopper.core.automation.UnparameterizedAutomation
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.toolkit.extensions.deletions
import com.greencopper.toolkit.extensions.insertions
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class AddToMyScheduleIfOnlyOneScheduleItem(
    private val myActivitiesManager: FavoritesManager<Long>,
    private val myScheduleManager: FavoritesManager<Long>,
    private val scheduleItemRepository: ScheduleItemRepository,
    private val scope: CoroutineScope,
    private val logger: Logging,
) : UnparameterizedAutomation() {

    override fun setup() {
        scope.launch {
            myActivitiesManager.favoriteIdsFlow.insertions().collect {
                it.forEach { addedActivityId -> handle(addedActivityId) }
            }
        }
    }

    private suspend fun handle(addedActivityId: Long) {
        try {
            scheduleItemRepository.getScheduleItemsForActivity(addedActivityId).first()
                .singleOrNull()
                ?.let {
                    myScheduleManager.addToFavorites(it)
                }
        } catch (error: Throwable) {
            logger.e(
                message = "Adding schedule item failed",
                throwable = error
            )
        }
    }

    companion object {
        val key: AutomationKey =
            AutomationKey("Event.MyActivities.AddToMyScheduleIfOnlyOneScheduleItem", 1)
    }
}

internal class RemoveFromMyScheduleIfOnlyOneScheduleItem(
    private val myActivitiesManager: FavoritesManager<Long>,
    private val myScheduleManager: FavoritesManager<Long>,
    private val scheduleItemRepository: ScheduleItemRepository,
    private val scope: CoroutineScope,
    private val logger: Logging,
) : UnparameterizedAutomation() {

    override fun setup() {
        scope.launch {
            myActivitiesManager.favoriteIdsFlow.deletions().collect {
                it.forEach { removedActivityId -> handle(removedActivityId) }
            }
        }
    }

    private suspend fun handle(removedActivityId: Long) {
        try {
            scheduleItemRepository.getScheduleItemsForActivity(removedActivityId).first()
                .singleOrNull()
                ?.let {
                    myScheduleManager.removeFromFavorites(it)
                }
        } catch (error: Throwable) {
            logger.e(
                message = "Removing schedule item failed",
                throwable = error
            )
        }
    }

    companion object {
        val key: AutomationKey =
            AutomationKey("Event.MyActivities.RemoveFromMyScheduleIfOnlyOneScheduleItem", 1)
    }
}
