package com.greencopper.event.activity.viewmodel

import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.core.text.HtmlCompat
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.ui.MyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.event.stage.Stage
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.eventmocks.*
import com.greencopper.interfacekit.tags.DisplayableTag
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.WidgetCollectionConfigurationHolder
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.extensions.getFormattedDateTime
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.FormatStyle

internal class ActivityDetailViewModelTest {
    private val activityRepository = MockActivityRepository()
    private val scheduleItem1: ScheduleItem = ScheduleItem(
        1,
        1,
        1,
        "scheduleItem",
        "subtitle",
        "description",
        listOf("photo"),
        listOf("tag"),
    )

    private val scheduleItem2: ScheduleItem = ScheduleItem(
        2,
        1,
        null,
        "scheduleItem",
        "subtitle",
        "description",
        listOf(),
        listOf(),
    )

    private val scheduleItem3: ScheduleItem = ScheduleItem(
        3,
        1,
        2,
        "scheduleItem",
        "subtitle",
        "description",
        listOf(),
        listOf(),
    )

    private val stage1 = Stage(
        1,
        "stageName",
        null,
        emptyList(),
        emptyList(),
        null,
    )

    private val timeSlotDate = ZonedDateTime.now()
    private val timeSlot = TimeSlot(
        1,
        1,
        timeSlotDate,
    )
    private val testTimedScheduleItem1: TimedScheduleItem = TimedScheduleItem(
        scheduleItem1,
        timeSlot,
        stage1,
    )
    private val testTimedScheduleItem2: TimedScheduleItem = TimedScheduleItem(
        scheduleItem2,
        timeSlot,
        null,
    )

    private val testTimedScheduleItem3: TimedScheduleItem = TimedScheduleItem(
        scheduleItem3,
        timeSlot,
        null
    )

    private val timedScheduleItemRepository = MockTimedScheduleItemRepository(
        listOf(
            testTimedScheduleItem1,
            testTimedScheduleItem2,
            testTimedScheduleItem3
        )
    )

    private val localizationService = MockLocalizationService()
    private val myScheduleManager = MockMyScheduleManager(setOf(1))
    private val widgetCollectionConfigurationHolder = WidgetCollectionConfigurationHolder()
    private val widgetCollectionResolver =
        WidgetCollectionResolver(widgetCollectionConfigurationHolder)
    private val widgetResolver = MockWidgetResolver()
    private val favoritesManager = MockFavoritesManager<Long>()

    private val mockZoneId = ZoneId.of("America/Phoenix")

    private val classUnderTest = ActivityDetailViewModel(
        activityRepository,
        timedScheduleItemRepository,
        localizationService,
        myScheduleManager,
        favoritesManager,
        widgetCollectionResolver,
        widgetResolver,
    )

    init {
        Toolkit.setupTest()
        bindSingleton<TimezoneProvider>(MockTimezoneProvider(mockZoneId))
        bindSingleton<LocalizationService>(localizationService)

        mockkStatic(HtmlCompat::class)
        val mockSpannable = MockSpannable("description")
        every { HtmlCompat.fromHtml(any(), any()) } returns mockSpannable

        mockkConstructor(SpannableStringBuilder::class)
        every { anyConstructed<SpannableStringBuilder>().length } returns mockSpannable.length
        every {
            anyConstructed<SpannableStringBuilder>().getSpans(
                any(),
                any(),
                BulletSpan::class.java
            )
        } returns emptyArray<BulletSpan>()
    }

    @Test
    fun getActivity_whenAllFieldsExists() {
        runTest {
            activityRepository.activities = listOf(
                ContentActivity(
                    1,
                    "name",
                    "subtitle",
                    "description",
                    listOf("photo"),
                    listOf("tag2", "tag1", "tag3"),
                )
            )

            val displayableTags = listOf(DisplayableTag("tag1"), DisplayableTag("tag2"))
            val detailViewData = classUnderTest.getActivity("screenName", 1, displayableTags, false).first()

            assertThat(
                DetailViewData<Long>(
                    1,
                    "name",
                    "subtitle",
                    SpannableStringBuilder("description"),
                    "photo",
                    listOf(
                        ScheduleItemViewData(
                            1,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            "stageName",
                            null,
                            true,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                1,
                                "scheduleItem",
                                null
                            ),
                        ),
                        ScheduleItemViewData(
                            2,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            null,
                            null,
                            false,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                2,
                                "scheduleItem",
                                null
                            ),
                        ),
                        ScheduleItemViewData(
                            3,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            null,
                            null,
                            false,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                3,
                                "scheduleItem",
                                null
                            ),
                        ),
                    ),
                    tags = displayableTags,
                    "activity_1_detail_primary"
                )
            ).usingRecursiveComparison().isEqualTo(
                detailViewData
            )
        }
    }

    @Test
    fun getActivity_whenSomeFieldsNulls() {
        runTest {
            val activity = ContentActivity(
                itemId = 1,
                name = "name",
                subtitle  = null,
                description = null,
                photos = listOf(),
                tags = listOf("tag"),
            )

            activityRepository.activities = listOf(activity)

            val detailViewData = classUnderTest.getActivity("screenName", 1, listOf(DisplayableTag("tag")), false).first()

            assertThat(
                DetailViewData<Long>(
                    1,
                    "name",
                    null,
                    null,
                    null,
                    listOf(
                        ScheduleItemViewData(
                            1,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            "stageName",
                            null,
                            true,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                1,
                                "scheduleItem",
                                null
                            ),
                        ),
                        ScheduleItemViewData(
                            2,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            null,
                            null,
                            false,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                2,
                                "scheduleItem",
                                null
                            ),
                        ),
                        ScheduleItemViewData(
                            3,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            null,
                            null,
                            false,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                3,
                                "scheduleItem",
                                null
                            ),
                        ),
                    ),
                    listOf(DisplayableTag("tag")),
                    "activity_1_detail_primary"
                )
            ).usingRecursiveComparison().isEqualTo(
                detailViewData
            )
        }
    }

    @Test
    fun getActivityDefaultName() {
        runTest {
            activityRepository.activities = listOf(
                ContentActivity(
                    1,
                    "name",
                    "subtitle",
                    "description",
                    listOf("photo"),
                    listOf("tag"),
                )
            )

            assertThat(classUnderTest.getActivityDefaultName(1)).isEqualTo(
                "name"
            )

            assertThat(classUnderTest.getActivityDefaultName(2)).isNull()
        }
    }

    @Test
    fun getWidgetItems_whenExists() {
        val instance = WidgetCollectionConfiguration.Instance(
            widgets = listOf(
                WidgetCollectionConfiguration.Instance.WidgetInfo(
                    WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
                    JsonArray(emptyList()),
                )
            )
        )
        widgetCollectionConfigurationHolder.currentConfiguration.value =
            WidgetCollectionConfiguration(
                mapOf(
                    "testKey" to instance
                )
            )
        val widgetItems = classUnderTest.getWidgetItems("testKey")
        val expectedResult = listOf(
            WidgetCollectionView.WidgetItem(
                WidgetCollectionConfiguration.Instance.WidgetKey("testKey", 1),
                MockWidgetParameters(),
            )
        )
        assertThat(widgetItems).usingRecursiveComparison().isEqualTo(expectedResult)

        // getWidgetItems second time should return exactly the same
        val widgetItemsSecondTime = classUnderTest.getWidgetItems("testKey")
        assertThat(widgetItemsSecondTime === widgetItems).isTrue
    }


    @Test
    fun getWidgetItems_whenNotExists() {
        val widgetItems = classUnderTest.getWidgetItems("testKey")
        assertThat(widgetItems.isEmpty()).isTrue
    }

    @Test
    fun addToFavoriteShouldSucceed() {
        val detailViewData = DetailViewData<Long>(
            1,
            "name",
            null,
            null,
            null,
            listOf(),
            listOf(DisplayableTag("tag")),
        )
        assertThat(favoritesManager.isInFavorites(1)).isFalse
        classUnderTest.addToFavorite(detailViewData)
        assertThat(favoritesManager.isInFavorites(1)).isTrue
    }

    @Test
    fun removeFromFavoriteShouldSucceed() {
        val detailViewData = DetailViewData<Long>(
            1,
            "name",
            null,
            null,
            null,
            listOf(),
            listOf(DisplayableTag("tag")),
        )
        classUnderTest.addToFavorite(detailViewData)
        assertThat(favoritesManager.isInFavorites(1)).isTrue
        classUnderTest.removeFromFavorite(detailViewData)
        assertThat(favoritesManager.isInFavorites(1)).isFalse
    }
}
