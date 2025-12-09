package com.greencopper.event.scheduleItem

import com.greencopper.core.automation.AutomationKey
import com.greencopper.core.automation.UnparameterizedAutomation
import com.greencopper.event.scheduleItem.data.repository.ScheduleItemRepository
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.favorites.toFavoriteable
import com.greencopper.toolkit.extensions.deletions
import com.greencopper.toolkit.extensions.insertions
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class AddToMyActivitiesIfOnlyScheduleItem(
    private val myActivitiesManager: FavoritesManager<Long>,
    private val myScheduleManager: FavoritesManager<Long>,
    private val scheduleItemRepository: ScheduleItemRepository,
    private val scope: CoroutineScope,
    private val logger: Logging,
) : UnparameterizedAutomation() {
    override fun setup() {
        scope.launch {
            myScheduleManager.favoriteIdsFlow.insertions().collect {
                it.forEach { addedScheduleItemId -> handle(addedScheduleItemId) }
            }
        }
    }

    private suspend fun handle(scheduleItemId: Long) {
        try {
            actOnActivities(Action.ADD, scheduleItemId, scheduleItemRepository, myActivitiesManager)
        } catch (error: Throwable) {
            logger.e(
                message = "Adding activity failed",
                throwable = error
            )
        }
    }

    companion object {
        val key: AutomationKey =
            AutomationKey("Event.MySchedule.AddToMyActivitiesIfOnlyScheduleItem", 1)
    }
}

internal class RemoveFromMyActivitiesIfOnlyScheduleItem(
    private val myActivitiesManager: FavoritesManager<Long>,
    private val myScheduleManager: FavoritesManager<Long>,
    private val scheduleItemRepository: ScheduleItemRepository,
    private val scope: CoroutineScope,
    private val logger: Logging,
) : UnparameterizedAutomation() {

    override fun setup() {
        scope.launch {
            myScheduleManager.favoriteIdsFlow.deletions().collect {
                it.forEach { removedScheduleItemId -> handle(removedScheduleItemId) }
            }
        }
    }

    private suspend fun handle(scheduleItemId: Long) {
        try {
            actOnActivities(Action.REMOVE, scheduleItemId, scheduleItemRepository, myActivitiesManager)
        } catch (error: Throwable) {
            logger.e(
                message = "Removing activity failed",
                throwable = error
            )
        }
    }

    companion object {
        val key: AutomationKey =
            AutomationKey("Event.MySchedule.RemoveFromMyActivitiesIfOnlyScheduleItem", 1)
    }
}

private enum class Action {
    ADD,
    REMOVE
}

private suspend fun actOnActivities(
    action: Action,
    scheduleItemId: Long,
    scheduleItemRepository: ScheduleItemRepository,
    myActivitiesManager: FavoritesManager<Long>,
) {
    val scheduleItem =
        scheduleItemRepository.getScheduleItemById(scheduleItemId).first() ?: return

    val isSingleItem = scheduleItemRepository
        .getScheduleItemsForActivity(scheduleItem.activityId)
        .first().size == 1

    if (isSingleItem) {
        val favorite = scheduleItem.activityId.toFavoriteable()
        when (action) {
            Action.ADD -> myActivitiesManager.addToFavorites(favorite)
            Action.REMOVE -> myActivitiesManager.removeFromFavorites(favorite)
        }
    }
}
