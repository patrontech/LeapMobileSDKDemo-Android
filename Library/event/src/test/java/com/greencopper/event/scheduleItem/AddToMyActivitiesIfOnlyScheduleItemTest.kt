package com.greencopper.event.scheduleItem

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

internal class AddToMyActivitiesIfOnlyScheduleItemTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val myActivitiesManager: MockFavoritesManager<Long> = MockFavoritesManager()
    private val myScheduleManager: MockFavoritesManager<Long> = MockFavoritesManager()
    private lateinit var scheduleItemRepository: MockScheduleItemRepository
    private lateinit var classUnderTest: AddToMyActivitiesIfOnlyScheduleItem

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
    @DisplayName("When adding scheduleItem of single item Activity to favorites, then Activity should be favorited")
    fun automationShouldAddToFavoriteIfSingle() = runTest {
        scheduleItemRepository = MockScheduleItemRepository(
            listOf(
                scheduleItems.first(),
                scheduleItems.last().copy(activityId = activity2Id)
            )
        )
        classUnderTest = AddToMyActivitiesIfOnlyScheduleItem(
            myActivitiesManager = myActivitiesManager,
            myScheduleManager = myScheduleManager,
            scheduleItemRepository = scheduleItemRepository,
            scope = testScope,
            logger = MockLogging(),
        )
        classUnderTest.setup()
        delay(500)

        myScheduleManager.addToFavorites(MockFavoriteable(schedule1Id))
        delay(500)

        myActivitiesManager.addToFavoritesCallCount shouldBe 1
        myActivitiesManager.removeFromFavoritesCallCount shouldBe 0
        myActivitiesManager.favoriteIds shouldBe setOf(activity1Id)
    }

    @Test
    @DisplayName("When adding scheduleItem of multiple items Activity to favorites, then nothing should happen")
    fun automationShouldDoNothingIfMultiple() = runTest {
        scheduleItemRepository = MockScheduleItemRepository(scheduleItems)
        classUnderTest = AddToMyActivitiesIfOnlyScheduleItem(
            myActivitiesManager = myActivitiesManager,
            myScheduleManager = myScheduleManager,
            scheduleItemRepository = scheduleItemRepository,
            scope = testScope,
            logger = MockLogging(),
        )

        classUnderTest.setup()
        delay(500)

        myScheduleManager.addToFavorites(MockFavoriteable(schedule1Id))
        delay(500)

        myActivitiesManager.addToFavoritesCallCount shouldBe 0
        myActivitiesManager.removeFromFavoritesCallCount shouldBe 0
        myActivitiesManager.favoriteIds shouldBe emptySet()
    }
}
