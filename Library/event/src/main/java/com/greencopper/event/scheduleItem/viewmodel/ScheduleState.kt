package com.greencopper.event.scheduleItem.viewmodel

import com.greencopper.core.content.serializers.ZonedDateTimeWithInstantSerializer
import com.greencopper.core.data.KiboSerializable
import com.greencopper.event.data.TimedScheduleItem
import com.greencopper.event.scheduleItem.ui.timeline.TimelineAdapter
import com.greencopper.interfacekit.empty.EmptyState
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.topbar.TopBarState
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.ZonedDateTime

@Serializable
internal data class ScheduleState(
    @Transient val layout: Layout? = null,
    @Transient val items: List<TimedScheduleItem> = emptyList(),
    val selectedSchedule: Set<SelectedSchedule> = emptySet(),
    val selectedView: SelectedView = SelectedView.List,
    val header: ViewState.HeaderState = ViewState.HeaderState(
        topBar = null,
        datePicker = null,
    ),
    @Transient val list: ViewState.ListState? = null,
    @Transient val timeline: ViewState.TimelineState? = null,
) : KiboSerializable<ScheduleState> {
    override fun getSerializer(): KSerializer<ScheduleState> = serializer()

    val isInMySchedule: Boolean get() = selectedSchedule.contains(SelectedSchedule.MySchedule)
    val isInMyInterests: Boolean get() = selectedSchedule.contains(SelectedSchedule.MyInterests)
}

internal object ViewState {

    @Serializable
    data class HeaderState(
        val topBar: TopBarState<ScheduleAction>? = null,
        val datePicker: DatePickerState? = null,
    )

    @Serializable
    data class DatePickerState(
        @Transient val dates: List<@Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime> = emptyList(),
        val selectedDate: @Serializable(with = ZonedDateTimeWithInstantSerializer::class) ZonedDateTime,
        val displayMode: DisplayMode,
    )

    interface ListState {
        class Empty(
            title: String,
            subtitle: String,
            imageName: String,
            topWidgetCollection: WidgetCollectionConfiguration.Instance?,
            screenName: String,
        ) : ListState, EmptyState(title, subtitle, imageName, topWidgetCollection, screenName)

        data class Content(val items: List<ScheduleListViewData>) : ListState
    }

    interface TimelineState {
        class Empty(
            title: String,
            subtitle: String,
            imageName: String,
            topWidgetCollection: WidgetCollectionConfiguration.Instance?,
            screenName: String,
        ) : TimelineState, EmptyState(title, subtitle, imageName, topWidgetCollection, screenName)

        data class Content(val items: List<TimelineAdapter.EventData>) : TimelineState
    }
}

internal enum class SelectedSchedule { MySchedule, MyInterests }

@Serializable
internal enum class SelectedView {
    @SerialName("list")
    List,

    @SerialName("timeline")
    Timeline,
}

@Serializable
internal enum class DisplayMode {
    @SerialName("daily")
    DAILY,

    @SerialName("monthly")
    MONTHLY,
}
