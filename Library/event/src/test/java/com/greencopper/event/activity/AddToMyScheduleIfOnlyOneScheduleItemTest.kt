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

internal class AddToMyScheduleIfOnlyOneScheduleItemTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val myActivitiesManager: MockFavoritesManager<Long> = MockFavoritesManager()
    private val myScheduleManager: MockFavoritesManager<Long> = MockFavoritesManager()
    private lateinit var scheduleItemRepository: MockScheduleItemRepository
    private lateinit var classUnderTest: AddToMyScheduleIfOnlyOneScheduleItem

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
    @DisplayName("When adding activity with single item to favorites, then item should be favorited")
    fun automationShouldAddToFavoriteIfSingle() = runTest {
        scheduleItemRepository = MockScheduleItemRepository(
            listOf(
                scheduleItems.first(),
                scheduleItems.last().copy(activityId = activity2Id)
            )
        )
        classUnderTest = AddToMyScheduleIfOnlyOneScheduleItem(
            myActivitiesManager = myActivitiesManager,
            myScheduleManager = myScheduleManager,
            scheduleItemRepository = scheduleItemRepository,
            scope = testScope,
            logger = MockLogging(),
        )
        classUnderTest.setup()
        delay(500)

        myActivitiesManager.addToFavorites(MockFavoriteable(activity1Id))
        delay(500)

        myScheduleManager.addToFavoritesCallCount shouldBe 1
        myScheduleManager.removeFromFavoritesCallCount shouldBe 0
        myScheduleManager.favoriteIds shouldBe setOf(schedule1Id)
    }

    @Test
    @DisplayName("When adding activity of multiple items to favorites, then nothing should happen")
    fun automationShouldDoNothingIfMultiple() = runTest {
        scheduleItemRepository = MockScheduleItemRepository(scheduleItems)
        classUnderTest = AddToMyScheduleIfOnlyOneScheduleItem(
            myActivitiesManager = myActivitiesManager,
            myScheduleManager = myScheduleManager,
            scheduleItemRepository = scheduleItemRepository,
            scope = testScope,
            logger = MockLogging(),
        )

        classUnderTest.setup()
        delay(500)

        myActivitiesManager.addToFavorites(MockFavoriteable(activity1Id))
        delay(500)

        myScheduleManager.addToFavoritesCallCount shouldBe 0
        myScheduleManager.removeFromFavoritesCallCount shouldBe 0
        myScheduleManager.favoriteIds shouldBe emptySet()
    }
}
