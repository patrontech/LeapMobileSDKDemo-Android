package com.greencopper.event.performers.viewmodel

import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.core.text.HtmlCompat
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.performers.Performer
import com.greencopper.event.scheduleItem.ScheduleItem
import com.greencopper.event.scheduleItem.ui.MyScheduleAnalytics
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.event.stage.Stage
import com.greencopper.event.timeSlot.TimeSlot
import com.greencopper.eventmocks.MockPerformerRepository
import com.greencopper.eventmocks.MockTimedScheduleItemRepository
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

internal class PerformerDetailViewModelTest {

    private val stage = Stage(
        1,
        "stageName",
        null,
        emptyList(),
        emptyList(),
        null,
    )

    private val scheduleItem1: ScheduleItem = ScheduleItem(
        1,
        1,
        null,
        "scheduleItem",
        "subtitle",
        "description",
        listOf("photo"),
        listOf("tag"),
    )

    private val scheduleItem2: ScheduleItem = ScheduleItem(
        2,
        1,
        1,
        "scheduleItem",
        "subtitle",
        "description",
        listOf(),
        listOf(),
        listOf("11"),
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

    private val timeSlotDate = ZonedDateTime.now()
    private val timeSlot = TimeSlot(
        1,
        1,
        timeSlotDate,
    )
    private val testTimedScheduleItem1: TimedScheduleItem = TimedScheduleItem(
        scheduleItem1,
        timeSlot,
    )
    private val testTimedScheduleItem2: TimedScheduleItem = TimedScheduleItem(
        scheduleItem2,
        timeSlot,
        stage,
    )

    private val testTimedScheduleItem3: TimedScheduleItem = TimedScheduleItem(
        scheduleItem3,
        timeSlot,
    )

    private val timedScheduleItemRepository = MockTimedScheduleItemRepository(
        listOf(
            testTimedScheduleItem1,
            testTimedScheduleItem2,
            testTimedScheduleItem3
        )
    )

    private val performerRepository = MockPerformerRepository()
    private val localizationService = MockLocalizationService()
    private val myScheduleManager = MockFavoritesManager<Long>(mutableSetOf(1))
    private val myActivitiesManager = MockFavoritesManager<Long>()
    private val myPerformersManager = MockFavoritesManager<String>()
    private val widgetCollectionConfigurationHolder = WidgetCollectionConfigurationHolder()
    private val widgetCollectionResolver =
        WidgetCollectionResolver(widgetCollectionConfigurationHolder)
    private val widgetResolver = MockWidgetResolver()

    private val mockZoneId = ZoneId.of("America/Phoenix")

    private val classUnderTest = PerformerDetailViewModel(
        performerRepository,
        timedScheduleItemRepository,
        localizationService,
        myScheduleManager,
        myActivitiesManager,
        myPerformersManager,
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
    fun getPerformer_whenAllFieldsExists() {
        runTest {
            performerRepository.performers = listOf(
                Performer(
                    itemId = "11",
                    name = "perf_name",
                    subtitle = "perf_subtitle",
                    description = "perf_description",
                    order = 3,
                    photos = listOf("perf_photo"),
                    tags = listOf("perf_tag3", "perf_tag1", "perf_tag2"),
                )
            )
            timedScheduleItemRepository.items = listOf(testTimedScheduleItem2)

            val displayableTags = listOf(DisplayableTag("perf_tag1"), DisplayableTag("perf_tag3"))
            val detailViewData = classUnderTest.getPerformer("screenName", "11", displayableTags, false).first()

            assertThat(
                DetailViewData(
                    itemId = "11",
                    name = "perf_name",
                    subtitle = "perf_subtitle",
                    description = SpannableStringBuilder("description"),
                    photo = "perf_photo",
                    scheduleItemList = listOf(
                        ScheduleItemViewData(
                            2,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            stage = stage.name,
                            null,
                            false,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                2,
                                "scheduleItem",
                                null
                            ),
                        ),
                    ),
                    tags = displayableTags,
                    widgetCollectionKey = "performer_11_detail_primary"
                )
            ).usingRecursiveComparison().isEqualTo(
                detailViewData
            )
        }
    }

    @Test
    fun getPerformer_whenSomeFieldsNulls() {
        runTest {
            performerRepository.performers = listOf(
                Performer(
                    itemId = "11",
                    name = "perf_name",
                    tags = listOf("tag")
                )
            )
            timedScheduleItemRepository.items = listOf(testTimedScheduleItem2)

            val detailViewData = classUnderTest.getPerformer("screenName", "11", listOf(DisplayableTag("tag")), false).first()

            assertThat(
                DetailViewData(
                    "11",
                    "perf_name",
                    null,
                    null,
                    null,
                    listOf(
                        ScheduleItemViewData(
                            2,
                            "scheduleItem",
                            timeSlotDate.getFormattedDateTime(FormatStyle.MEDIUM, null, mockZoneId),
                            null,
                            stage = stage.name,
                            null,
                            false,
                            MyScheduleAnalytics.Data(
                                "screenName",
                                2,
                                "scheduleItem",
                                null
                            ),
                        ),
                    ),
                    listOf(DisplayableTag("tag")),
                    "performer_11_detail_primary"
                )
            ).usingRecursiveComparison().isEqualTo(
                detailViewData
            )
        }
    }

    @Test
    fun getPerformerDefaultName() {
        runTest {
            performerRepository.performers = listOf(
                Performer(
                    itemId = "11",
                    name = "perf_name"
                )
            )

            assertThat(classUnderTest.getPerformerDefaultName("11")).isEqualTo(
                "perf_name"
            )

            assertThat(classUnderTest.getPerformerDefaultName("22")).isNull()
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
        val detailViewData = DetailViewData(
            "11",
            "name",
            null,
            null,
            null,
            listOf(),
            listOf(DisplayableTag("tag")),
        )
        assertThat(myPerformersManager.isInFavorites("11")).isFalse
        classUnderTest.addToFavorite(detailViewData)
        assertThat(myPerformersManager.isInFavorites("11")).isTrue
    }

    @Test
    fun removeFromFavoriteShouldSucceed() {
        val detailViewData = DetailViewData(
            "11",
            "name",
            null,
            null,
            null,
            listOf(),
            listOf(DisplayableTag("tag")),
        )
        classUnderTest.addToFavorite(detailViewData)
        assertThat(myPerformersManager.isInFavorites("11")).isTrue
        classUnderTest.removeFromFavorite(detailViewData)
        assertThat(myPerformersManager.isInFavorites("11")).isFalse
    }
}
