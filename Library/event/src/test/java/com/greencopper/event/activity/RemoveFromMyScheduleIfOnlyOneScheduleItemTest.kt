package com.greencopper.event.activity

import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.eventmocks.MockScheduleItemRepository
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.interfacekit.MockFavoriteable
import com.greencopper.testmocks.interfacekit.MockFavoritesManager
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.toolkit.MockLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class RemoveFromMyScheduleIfOnlyOneScheduleItemTest :
    CoroutineTest(UnconfinedTestDispatcher()) {

    private val myActivitiesManager: MockFavoritesManager<Long> by lazy { MockFavoritesManager(initFavActivities.toMutableSet()) }
    private val myScheduleManager: MockFavoritesManager<Long> by lazy { MockFavoritesManager(initFavScheduleItems.toMutableSet()) }
    private lateinit var scheduleItemRepository: MockScheduleItemRepository
    private lateinit var classUnderTest: RemoveFromMyScheduleIfOnlyOneScheduleItem

    var initFavActivities = setOf<Long>()
    var initFavScheduleItems = setOf<Long>()

    private val schedule1Id = 1L
    private val schedule2Id = 2L
    private val activity1Id = 100L
    private val activity2Id = 200L

    private val scheduleItems = listOf(
        ScheduleItem(
            itemId = schedule1Id,
            activityId = activity1Id,
            stageId = 1,
            "name",
            "subtitle",
            "description",
            photos = listOf("photo"),
            tags = emptyList(),
        ),
        ScheduleItem(
            itemId = schedule2Id,
            activityId = activity1Id,
            stageId = null,
            "name2",
            subtitle = null,
            description = null,
            photos = emptyList(),
            tags = emptyList(),
        ),
    )

    override fun afterEach() {}

    @Test
    @DisplayName("When removing activity with single item from favorites, then item should be unfavorited")
    fun automationShouldRemoveFromFavoriteIfSingle() = runTest {
        initFavScheduleItems = setOf(schedule1Id)
        initFavActivities = setOf(activity1Id)
        scheduleItemRepository = MockScheduleItemRepository(
            listOf(
                scheduleItems.first(),
                scheduleItems.last().copy(activityId = activity2Id)
            )
        )
        classUnderTest = RemoveFromMyScheduleIfOnlyOneScheduleItem(
            myActivitiesManager = myActivitiesManager,
            myScheduleManager = myScheduleManager,
            scheduleItemRepository = scheduleItemRepository,
            scope = testScope,
            logger = MockLogging(),
        )
        classUnderTest.setup()
        delay(500)

        myActivitiesManager.removeFromFavorites(MockFavoriteable(activity1Id))
        delay(500)

        myScheduleManager.addToFavoritesCallCount shouldBe 0
        myScheduleManager.removeFromFavoritesCallCount shouldBe 1
        myScheduleManager.favoriteIds shouldBe setOf()
    }

    @Test
    @DisplayName("When removing activity with single item from favorites, then nothing should happen")
    fun automationShouldDoNothingIfMultiple() = runTest {
        initFavScheduleItems = setOf(schedule1Id)
        initFavActivities = setOf(activity1Id)
        scheduleItemRepository = MockScheduleItemRepository(scheduleItems)
        classUnderTest = RemoveFromMyScheduleIfOnlyOneScheduleItem(
            myActivitiesManager = myActivitiesManager,
            myScheduleManager = myScheduleManager,
            scheduleItemRepository = scheduleItemRepository,
            scope = testScope,
            logger = MockLogging(),
        )

        classUnderTest.setup()
        delay(500)

        myActivitiesManager.removeFromFavorites(MockFavoriteable(activity1Id))
        delay(500)

        myScheduleManager.addToFavoritesCallCount shouldBe 0
        myScheduleManager.removeFromFavoritesCallCount shouldBe 0
        myScheduleManager.favoriteIds shouldBe setOf(schedule1Id)
    }
}
