package com.greencopper.event.scheduleItem.viewmodel

import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import androidx.core.text.HtmlCompat
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.activity.ui.viewdata.DetailViewData
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
import com.greencopper.testmocks.interfacekit.MockWidgetParameters
import com.greencopper.testmocks.interfacekit.MockWidgetResolver
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

internal class ScheduleItemDetailViewModelTest {
    private val date = ZonedDateTime.now()

    private val scheduleItemRepository = MockScheduleItemRepository(
        listOf(
            ScheduleItem(
                itemId = 1,
                activityId = 1,
                stageId = 1,
                "name",
                "subtitle",
                "description",
                photos = listOf("photo"),
                tags = listOf("tag3", "tag1", "tag2"),
            ),
            ScheduleItem(
                itemId = 2,
                activityId = 1,
                stageId = null,
                "name2",
                subtitle = null,
                description = null,
                photos = emptyList(),
                tags = listOf("tag1", "tag3", "tag2"),
            ),
        )
    )
    private val timeSlotRepository = MockTimeSlotRepository(
        listOf(
            TimeSlot(
                id = 1, scheduleItemId = 1, dayOfEvent = date, startDate = date, endDate = date.plusHours(1)
            )
        )
    )
    private val stageRepository = MockStageRepository(
        listOf(
            Stage(
                id = 1,
                "stageName",
                "subtitle",
                photos = emptyList(),
                tags = emptyList(),
                "stageDetailLink"
            )
        )
    )
    private val displayableTags = listOf(DisplayableTag("tag3"), DisplayableTag("tag1"), DisplayableTag("unused"))
    private val displayedTags = listOf(DisplayableTag("tag3"), DisplayableTag("tag1"))

    private val myScheduleManager = MockMyScheduleManager(setOf(2))
    private val widgetCollectionConfigurationHolder = WidgetCollectionConfigurationHolder()
    private val widgetCollectionResolver = WidgetCollectionResolver(widgetCollectionConfigurationHolder)
    private val widgetResolver = MockWidgetResolver()
    private val timezoneProvider: MockTimezoneProvider

    private val classUnderTest = ScheduleItemDetailViewModel(
        scheduleItemRepository,
        timeSlotRepository,
        stageRepository,
        myScheduleManager,
        widgetCollectionResolver,
        widgetResolver,
    )

    init {
        Toolkit.setupTest()
        bindSingleton<LocalizationService>(MockLocalizationService())
        timezoneProvider = MockTimezoneProvider(ZoneId.of("America/Phoenix"))
        bindProvider<TimezoneProvider>(timezoneProvider)

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
    fun getScheduleDetailItem_shouldReturnItem() {
        runTest {
            val detailItem1 = classUnderTest.getScheduleDetailItem(
                "screenName",
                1,
                displayableTags,
                false,
            ).first()
            val time = date.getFormattedDateTime(
                null,
                FormatStyle.SHORT,
                timezoneProvider.zoneId
            ) + " - " + date.plusHours(1).getFormattedDateTime(
                null,
                FormatStyle.SHORT,
                timezoneProvider.zoneId
            )
            assertThat(detailItem1)
                .usingRecursiveComparison()
                .isEqualTo(
                    DetailViewData<Long>(
                        itemId = 1,
                        name = "name",
                        subtitle = "subtitle",
                        description = SpannableStringBuilder("description"),
                        photo = "photo",
                        scheduleItemList = listOf(
                            ScheduleItemViewData(
                                itemId = 1,
                                name = "name",
                                dayOfEvent = date.getFormattedDateTime(
                                    FormatStyle.MEDIUM,
                                    null,
                                    timezoneProvider.zoneId
                                ),
                                timeOfEvent = time,
                                stage = "stageName",
                                stageDetailLink = "stageDetailLink",
                                isInMySchedule = false,
                                MyScheduleAnalytics.Data(
                                    "screenName",
                                    1,
                                    "name",
                                    date,
                                ),
                            )
                        ),
                        tags = displayedTags,
                        widgetCollectionKey = "scheduleItem_1_detail_primary",
                    )
                )

            val detailItem2 = classUnderTest.getScheduleDetailItem(
                "screenName",
                2,
                displayableTags,
                false,
            ).first()
            assertThat(detailItem2)
                .usingRecursiveComparison()
                .isEqualTo(
                    DetailViewData<Long>(
                        itemId = 2,
                        name = "name2",
                        subtitle = null,
                        description = null,
                        photo = null,
                        scheduleItemList = listOf(
                            ScheduleItemViewData(
                                itemId = 2,
                                name = "name2",
                                dayOfEvent = null,
                                timeOfEvent = null,
                                stage = null,
                                stageDetailLink = null,
                                isInMySchedule = true,
                                MyScheduleAnalytics.Data(
                                    "screenName",
                                    2,
                                    "name2",
                                    null
                                ),
                            )
                        ),
                        tags = displayedTags,
                        widgetCollectionKey = "scheduleItem_2_detail_primary",
                    )
                )
        }
    }

    @Test
    fun getScheduleDetailItem_withHiddenDate_shouldReturnItem() {
        runTest {
            val detailItem1 = classUnderTest.getScheduleDetailItem(
                "screenName",
                1,
                displayableTags,
                true,
                ).first()
            assertThat(detailItem1)
                .usingRecursiveComparison()
                .isEqualTo(
                    DetailViewData<Long>(
                        itemId = 1,
                        name = "name",
                        subtitle = "subtitle",
                        description = SpannableStringBuilder("description"),
                        photo = "photo",
                        scheduleItemList = listOf(
                            ScheduleItemViewData(
                                itemId = 1,
                                name = "name",
                                dayOfEvent = date.getFormattedDateTime(
                                    FormatStyle.MEDIUM,
                                    null,
                                    timezoneProvider.zoneId
                                ),
                                timeOfEvent = date.getFormattedDateTime(
                                    null,
                                    FormatStyle.SHORT,
                                    timezoneProvider.zoneId
                                ),
                                stage = "stageName",
                                stageDetailLink = "stageDetailLink",
                                isInMySchedule = false,
                                MyScheduleAnalytics.Data(
                                    "screenName",
                                    1,
                                    "name",
                                    date
                                ),
                            )
                        ),
                        tags = displayedTags,
                        widgetCollectionKey = "scheduleItem_1_detail_primary"
                    )
                )
        }
    }

    @Test
    fun getScheduleItemDefaultName_shouldReturnName() {
        runTest {
            assertThat(classUnderTest.getScheduleItemDefaultName(1))
                .isEqualTo("name")

            assertThat(classUnderTest.getScheduleItemDefaultName(2))
                .isEqualTo("name2")

            assertThat(classUnderTest.getScheduleItemDefaultName(3))
                .isNull()
        }
    }


    @Test
    fun getWidgetItems_whenNoCollection_shouldReturnEmptyList() {
        val resultItems = classUnderTest.getWidgetItems("")
        assertThat(resultItems).isEmpty()
        // second time
        assertThat(classUnderTest.getWidgetItems("")).isEmpty()
    }

    @Test
    fun getWidgets_whenCollectionIsEmpty_shouldReturnEmptyList() {
        val widgetCollectionKey = "testKey"
        val testWidgetCollectionInstance = WidgetCollectionConfiguration.Instance(
            widgets = emptyList()
        )
        widgetCollectionConfigurationHolder.currentConfiguration.value = WidgetCollectionConfiguration(
            mapOf(widgetCollectionKey to testWidgetCollectionInstance)
        )
        val resultItems = classUnderTest.getWidgetItems(widgetCollectionKey)
        assertThat(resultItems).isEmpty()
    }

    @Test
    fun getWidgets_whenCollectionHasElements_shouldReturnElements() {
        val widgetCollectionKey = "testKey"
        val testWidgetCollectionInstance = WidgetCollectionConfiguration.Instance(
            widgets = listOf(
                WidgetCollectionConfiguration.Instance.WidgetInfo(
                    WidgetCollectionConfiguration.Instance.WidgetKey(name = widgetCollectionKey, version = 1),
                    JsonArray(emptyList()),
                )
            )
        )
        widgetCollectionConfigurationHolder.currentConfiguration.value = WidgetCollectionConfiguration(
            mapOf(widgetCollectionKey to testWidgetCollectionInstance)
        )
        val resultItems = classUnderTest.getWidgetItems(widgetCollectionKey)
        assertThat(resultItems)
            .usingRecursiveComparison()
            .isEqualTo(
                listOf(
                    WidgetCollectionView.WidgetItem(
                        key = WidgetCollectionConfiguration.Instance.WidgetKey(
                            name = widgetCollectionKey,
                            version = 1
                        ),
                        params = MockWidgetParameters()
                    )
                )
            )

        // check instance the same second time
        val resultItemsSecondTime = classUnderTest.getWidgetItems("")
        assertThat(resultItemsSecondTime == resultItems)
            .isTrue
    }
}
