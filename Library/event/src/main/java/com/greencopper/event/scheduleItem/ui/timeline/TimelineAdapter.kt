package com.greencopper.event.scheduleItem.ui.timeline

import android.graphics.Color
import android.graphics.Rect
import android.util.SparseArray
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.core.util.forEach
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.*
import com.greencopper.event.scheduleItem.ui.utils.isAfterOrEqualTo
import com.greencopper.event.scheduleItem.ui.utils.isBeforeOrEqualTo
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.toolkit.extensions.getFormattedDateTime
import kotlinx.coroutines.*
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.minutes

/**
 * This Adapter works in conjunction with [TimelineLayoutManager].
 * Instead of providing a "linear list" of item and let a LinearLayoutManager do the job,
 * we have a list of item containing the coordinates of the view within the whole canvas of the RecyclerView.
 * The LayoutManager is then in charge of going through this list and determine which view should be added.
 */
internal class TimelineAdapter(
    val origin: Layout,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Important lingo
     *
     * itemAttribute: see [ItemAttributes]
     * position: the key which refer to a itemAttribute in [itemAttributesList]
     * canvas: represents the entire space where all views are virtually laid out, beyond the RecyclerView's visible window
     * section: A stage and its events
     */

    private lateinit var timezoneProvider: TimezoneProvider
    private val colors: EventColor.ScheduleList.Timeline by lazy { EventColor.schedule.timeline }
    private var currentData = listOf<EventData>()
    private var recyclerView: RecyclerView? = null
    private val layoutInflater by lazy { origin.layoutInflater }

    /** The actual collection used by the RecyclerView. Each element represents a RecyclerView's item.
     * @see [ItemAttributes]
     */
    internal var itemAttributesList: SparseArray<ItemAttributes> = SparseArray()

    /** A LinkedHashMap containing (NameOfStage, LinesInStage(EventsInLine)) */
    private var itemsByStage: LinkedHashMap<String?, MutableList<MutableList<EventData>>> = LinkedHashMap()

    /** Data for hours in hourline per position. Used for binding */
    private val hoursTextPerPosition = SparseArray<String>()

    /** Data for events per position. Used for binding */
    private val eventsPerPosition = SparseArray<EventData>()

    /** Data for stages per position. Used for binding */
    private val stagesPerPosition = SparseArray<EventData>()

    /** Keeps the initial coordinates of stages calculated in [prepareLayout] */
    internal val initialItemAttributesStagesPerPosition = SparseArray<ItemAttributes>()

    //region Common dimensions
    private val hourHeaderHeight by lazy { origin.resources.getDimensionPixelSize(R.dimen.timeline_hourline_height) }
    private val hourSeparatorWidth by lazy { 2.dpToPx() }
    private val screenHeight by lazy { origin.resources.displayMetrics.heightPixels }
    private val screenWidth by lazy { origin.resources.displayMetrics.widthPixels }
    var contentWidth = 0
        private set
    var contentHeight = 0
        private set

    /** Represent the top coordinate of the "scrolling content", which currently is the bottom of the hourline. Mainly used by the LayoutManager */
    internal val virtualTop: Int = hourHeaderHeight
    //endregion

    init {
        setHasStableIds(true)
    }

    fun setup(
        timeToWidthRatio: Int,
        favoritesEditing: FavoritesEditing?,
        onScheduleItemClicked: (EventData) -> Unit,
        onAddRemoveMyScheduleItemClicked: (EventData) -> Unit,
        timezoneProvider: TimezoneProvider,
        lifecycleScope: LifecycleCoroutineScope,
    ) {
        this.lifecycleScope = lifecycleScope
        this.timezoneProvider = timezoneProvider
        this.minutesToWidthScreenRatio = timeToWidthRatio
        this.favoritesEditing = favoritesEditing
        this.onEventClicked = onScheduleItemClicked
        this.onAddRemoveMyScheduleItemClicked = onAddRemoveMyScheduleItemClicked
    }

    fun setEvents(data: List<EventData>) {
        if (TimelineDiffUtil.shouldRedrawAll(data, this.currentData)) {
            startDate = getStartDate(data)
            endDate = getEndDate(data)
            organize(data)
            prepareLayout()

            recyclerView?.doOnNextLayout {
                resetScroll()
            }
        } else {
            data.forEach {
                eventsPerPosition.forEach loop@{ key, value ->
                    if (it.id == value.id && !TimelineDiffUtil.areContentsTheSame(it, value)) {
                        eventsPerPosition[key] = it
                        notifyItemChanged(key)
                        return@loop
                    }
                }
            }
        }

        this.currentData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (Type.getFromOrdinal(viewType)) {
            Type.EVENT -> TimelineEventHolder(TimelineEventViewBinding.inflate(layoutInflater, parent, false))
            Type.STAGE -> TimelineStageHolder(TimelineStageViewBinding.inflate(layoutInflater, parent, false))
            Type.HOUR -> TimelineHourHolder(TimelineHourViewBinding.inflate(layoutInflater, parent, false)).apply {
                setup()
            }

            Type.HOUR_SEPARATOR -> TimelineHourSeparatorHolder(parent.context).apply { setup() }
            Type.HOUR_BACKGROUND -> TimelineHourBackgroundHolder(parent.context).apply { setup() }
            Type.BACKGROUND_PANEL -> TimelineBackgroundPanelHolder(parent.context)
            Type.TIMELINE_CURSOR -> TimelineCurrentCursorHolder(parent.context).apply { setup() }
            Type.HOURLINE_CURSOR -> TimelineHourlineCursorHolder(parent.context).apply { setup() }
        }
    }

    private var onEventClicked: (EventData) -> Unit = {}
    private var favoritesEditing: FavoritesEditing? = null
    private var onAddRemoveMyScheduleItemClicked: (EventData) -> Unit = {}
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (Type.getFromOrdinal(getItemViewType(position))) {
            Type.HOUR -> {
                val timelineHourHolder: TimelineHourHolder = holder as TimelineHourHolder
                timelineHourHolder.bind(hoursTextPerPosition[position, ""])
            }

            Type.STAGE -> {
                val stage: EventData = stagesPerPosition[position]
                val headerHolder: TimelineStageHolder = holder as TimelineStageHolder
                headerHolder.setup(stage.stageLabel ?: "Unknown")
            }

            Type.EVENT -> {
                val show: EventData = eventsPerPosition[position]
                (holder as TimelineEventHolder).setup(
                    show,
                    favoritesEditing = favoritesEditing,
                    lifecycleScope = origin.lifecycleScope,
                    timezoneProvider = timezoneProvider,
                    onItemClick = onEventClicked,
                    onAddRemoveMyScheduleItemClicked = onAddRemoveMyScheduleItemClicked
                )
            }

            Type.BACKGROUND_PANEL -> {
                val backgroundHolder = holder as TimelineBackgroundPanelHolder
                backgroundHolder.bind(
                    when (position) {
                        backgroundPanelBeforePosition -> {
                            colors.background.past
                        }

                        backgroundPanelAfterPosition -> {
                            colors.background.future
                        }

                        else -> Color.TRANSPARENT
                    }
                )
            }

            else -> {}
        }
    }

    private fun prepareLayout() {
        cursorUpdateJob?.cancel()
        itemAttributesList.clear()
        hoursTextPerPosition.clear()
        eventsPerPosition.clear()
        stagesPerPosition.clear()
        initialItemAttributesStagesPerPosition.clear()

        if (itemsByStage.isEmpty()) {
            recyclerView?.requestLayout()
            return
        }

        var itemAttributesPosition = 0
        itemAttributesPosition = setupStagesAndEvents(itemAttributesPosition)
        itemAttributesPosition = setupHourlineAndSeparators(itemAttributesPosition)
        setupBackgroundAndCursor(itemAttributesPosition)
    }

    //region Stages and events
    internal val stageHeight by lazy { 35.dpToPx() }
    private val bottomMargin by lazy { 16.dpToPx() }
    private val verticalMarginBetweenItems by lazy { 12.dpToPx() }

    private fun setupStagesAndEvents(itemAttributesPosition: Int): Int {
        var position = itemAttributesPosition
        var lastShowRectBottom = 0
        val showViewHolder = TimelineEventHolder(TimelineEventViewBinding.inflate(layoutInflater))
        var top = hourHeaderHeight
        itemsByStage.entries.forEachIndexed { sectionIndex, entry ->
            val scheduleItemsLines: List<List<EventData>> = entry.value

            // Stage cell
            entry.key?.let {
                val stageRectLeft = 0
                if (sectionIndex != 0) top += verticalMarginBetweenItems
                val stageRectBottom = top + stageHeight
                val stageRect = Rect(stageRectLeft, top, screenWidth, stageRectBottom)
                val stageItemAttributes = ItemAttributes(
                    Type.STAGE,
                    stageRect,
                    isOffsetEnabledHorizontally = false,
                    isOffsetEnabledVertically = true
                )
                itemAttributesList.put(position, stageItemAttributes)
                stagesPerPosition.put(position, scheduleItemsLines[0][0])
                initialItemAttributesStagesPerPosition.put(position, stageItemAttributes.copy())
                position++
                top = stageRectBottom
            }

            // Show cells
            scheduleItemsLines.forEach { eventsOfLine ->
                var maxLineHeight = 0

                val showRectTop = top + verticalMarginBetweenItems
                eventsOfLine.forEach { eventData ->
                    val showRectLeft = horizontalOffsetOf(eventData.startDate)
                    var showRectRight = horizontalOffsetOf(eventData.computedEndDate)
                    showRectRight -= 2 // width is shortened slightly so events with the same start and end time don't overlap

                    showViewHolder.minimalSetup(
                        eventData,
                        timezoneProvider,
                    )
                    val width = MeasureSpec.makeMeasureSpec(showRectRight - showRectLeft, MeasureSpec.EXACTLY)
                    showViewHolder.itemView.measure(width, MeasureSpec.makeMeasureSpec(999, MeasureSpec.AT_MOST))
                    val measuredHeight = showViewHolder.itemView.measuredHeight

                    val showRectBottom = showRectTop + measuredHeight
                    val showRect = Rect(
                        showRectLeft, showRectTop, showRectRight, showRectBottom
                    )
                    lastShowRectBottom = maxOf(lastShowRectBottom, showRectBottom)
                    val showItemAttributes = ItemAttributes(Type.EVENT, showRect)
                    itemAttributesList.put(position, showItemAttributes)

                    eventsPerPosition.put(position, eventData)
                    maxLineHeight = maxOf(maxLineHeight, measuredHeight)
                    position++
                }

                top += maxLineHeight + verticalMarginBetweenItems
            }
        }

        contentHeight = lastShowRectBottom + bottomMargin
        contentWidth = horizontalOffsetOf(endDate)
        return position
    }
    //endregion

    //region Hourline and separators
    private fun setupHourlineAndSeparators(itemAttributesPosition: Int): Int {
        var position = itemAttributesPosition
        val backgroundHourlineRect = Rect(0, 0, screenWidth, hourHeaderHeight)
        itemAttributesList.put(
            position++, ItemAttributes(
                Type.HOUR_BACKGROUND,
                backgroundHourlineRect,
                isOffsetEnabledHorizontally = false,
                isOffsetEnabledVertically = false
            )
        )

        val hourViewHolder = TimelineHourHolder(TimelineHourViewBinding.inflate(layoutInflater)).apply { setup() }
        fun getHourViewWidth(hourString: String): Int {
            hourViewHolder.bind(hourString)
            hourViewHolder.itemView.measure(
                MeasureSpec.makeMeasureSpec(999, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(hourHeaderHeight, MeasureSpec.EXACTLY)
            )
            return hourViewHolder.itemView.measuredWidth
        }

        // Hour cells and Hour Separator cells
        fun addHourText(startOffset: Int, endOffset: Int, text: String) {
            val hourRect = Rect(
                startOffset, 0, endOffset, hourHeaderHeight
            )
            val hourHeaderItemAttributes = ItemAttributes(
                Type.HOUR,
                hourRect,
                isOffsetEnabledHorizontally = true,
                isOffsetEnabledVertically = false
            )
            itemAttributesList.put(position, hourHeaderItemAttributes)
            hoursTextPerPosition.put(position++, text)
        }

        var hour: ZonedDateTime = startDate.plusHours(1).withMinute(0).withSecond(0)
        while (hour.isBefore(endDate)) {
            val startOffset = horizontalOffsetOf(hour)

            val separatorRect = Rect(startOffset, 0, startOffset + hourSeparatorWidth, screenHeight)
            val separatorItemAttributes = ItemAttributes(
                Type.HOUR_SEPARATOR,
                separatorRect,
                isOffsetEnabledHorizontally = true,
                isOffsetEnabledVertically = false
            )
            itemAttributesList.put(position++, separatorItemAttributes)

            val hourString = hour.getFormattedDateTime(null, FormatStyle.SHORT, timezoneProvider.zoneId)
            val hourViewWidth = getHourViewWidth(hourString)
            val hourViewStartOffset = maxOf(startOffset - hourViewWidth / 2 + hourSeparatorWidth / 2, 8.dpToPx())
            addHourText(
                hourViewStartOffset, hourViewStartOffset + hourViewWidth, hourString
            )

            hour = hour.plusHours(1)
        }
        return position
    }
    //endregion

    //region Background panels and current cursor
    private var backgroundPanelBeforePosition = -1
    private var backgroundPanelAfterPosition = -1
    private var timelineCursorPosition = -1
    private var hourlineCursorPosition = -1
    private lateinit var lifecycleScope: LifecycleCoroutineScope
    private var cursorUpdateJob: Job? = null

    private fun setupBackgroundAndCursor(itemAttributesPosition: Int): Int {
        var position = itemAttributesPosition
        backgroundPanelBeforePosition = position++
        val invisibleRect = Rect(0, 0, 0, 0)
        itemAttributesList.put(
            backgroundPanelBeforePosition,
            ItemAttributes(
                Type.BACKGROUND_PANEL,
                Rect(invisibleRect),
                isOffsetEnabledHorizontally = true,
                isOffsetEnabledVertically = false
            )
        )
        backgroundPanelAfterPosition = position++
        itemAttributesList.put(
            backgroundPanelAfterPosition,
            ItemAttributes(
                Type.BACKGROUND_PANEL,
                Rect(invisibleRect),
                isOffsetEnabledHorizontally = true,
                isOffsetEnabledVertically = false
            )
        )
        timelineCursorPosition = position++
        itemAttributesList.put(
            timelineCursorPosition,
            ItemAttributes(
                Type.TIMELINE_CURSOR,
                Rect(invisibleRect),
                isOffsetEnabledHorizontally = true,
                isOffsetEnabledVertically = false
            )
        )
        hourlineCursorPosition = position++
        itemAttributesList.put(
            hourlineCursorPosition,
            ItemAttributes(
                Type.HOURLINE_CURSOR,
                Rect(invisibleRect),
                isOffsetEnabledHorizontally = true,
                isOffsetEnabledVertically = false
            )
        )

        updateBackgroundAndCursor()
        return position
    }

    private fun updateBackgroundAndCursor() {
        cursorUpdateJob = lifecycleScope.launch {
            val now = getTimeNow()
            val invisible = Rect(0, 0, 0, 0)
            if (now.isBefore(startDate)) {
                itemAttributesList[backgroundPanelBeforePosition].rect.set(invisible)
                itemAttributesList[timelineCursorPosition].rect.set(invisible)
                itemAttributesList[hourlineCursorPosition].rect.set(invisible)
                itemAttributesList[backgroundPanelAfterPosition].rect.set(Rect(0, 0, contentWidth, screenHeight))
            } else if (now.isAfter(endDate)) {
                itemAttributesList[backgroundPanelAfterPosition].rect.set(invisible)
                itemAttributesList[timelineCursorPosition].rect.set(invisible)
                itemAttributesList[hourlineCursorPosition].rect.set(invisible)
                itemAttributesList[backgroundPanelBeforePosition].rect.set(Rect(0, 0, contentWidth, screenHeight))
            } else {
                val horizontalOffsetOfCurrent = horizontalOffsetOf(now)
                val hourSeparatorWidthHalf = hourSeparatorWidth / 2
                val hourlineCursorWidthHalf =
                    origin.resources.getDimensionPixelSize(R.dimen.timeline_arrow_cursor_width) / 2

                itemAttributesList[backgroundPanelBeforePosition].rect.set(
                    Rect(0, 0, horizontalOffsetOfCurrent, screenHeight)
                )
                itemAttributesList[backgroundPanelAfterPosition].rect.set(
                    Rect(horizontalOffsetOfCurrent, 0, contentWidth, screenHeight)
                )
                itemAttributesList[timelineCursorPosition].rect.set(
                    Rect(
                        horizontalOffsetOfCurrent - hourSeparatorWidthHalf,
                        0,
                        horizontalOffsetOfCurrent + hourSeparatorWidthHalf,
                        screenHeight
                    )
                )
                itemAttributesList[hourlineCursorPosition].rect.set(
                    Rect(
                        horizontalOffsetOfCurrent - hourlineCursorWidthHalf,
                        0,
                        horizontalOffsetOfCurrent + hourlineCursorWidthHalf,
                        hourHeaderHeight
                    )
                )
            }

            recyclerView?.requestLayout()
            delay(1.minutes)

            updateBackgroundAndCursor()
        }
    }
    //endregion

    /**
     * Organize events by their respective stage
     */
    private fun organize(events: List<EventData>) {
        val result = linkedMapOf<String?, MutableList<MutableList<EventData>>>()
        if (events.isEmpty()) {
            itemsByStage = result
            return
        }

        /** Get representations of lines of events for a stage */
        fun getFromMap(stage: String?): MutableList<MutableList<EventData>> {
            return result[stage] ?: mutableListOf<MutableList<EventData>>().also {
                result[stage] = it
            }
        }
        getFromMap(null)

        val eventsSorted =
            events.sortedWith(compareBy<EventData, Int?>(nullsLast()) { it.stageOrder }.thenBy { it.startDate }
                .thenBy { it.stageLabel?.lowercase() })

        //We place events in their section. For that, we put each event in their section, checking for the first line where it could fit, or creating a new one if necessary
        eventsSorted.forEach rootLoop@{ event ->
            val lines = getFromMap(event.stageLabel)

            lines.forEach { line ->
                if (line.size == 1) {

                    if (line[0].startDate.isAfterOrEqualTo(event.computedEndDate)) {
                        line.add(0, event)
                        return@rootLoop
                    } else if (line[0].computedEndDate.isBeforeOrEqualTo(event.startDate)) {
                        line.add(event)
                        return@rootLoop
                    }

                    return@forEach
                } else {
                    val iterator = line.listIterator()
                    var next = iterator.next()

                    if (next.startDate.isAfterOrEqualTo(event.computedEndDate)) {
                        line.add(iterator.previousIndex(), event)
                        return@rootLoop
                    }

                    var previous: EventData
                    while (iterator.hasNext()) {
                        previous = next
                        next = iterator.next()

                        if (previous.computedEndDate.isBeforeOrEqualTo(event.startDate) && next.startDate.isAfterOrEqualTo(
                                event.computedEndDate
                            )
                        ) {
                            line.add(iterator.previousIndex(), event)
                            return@rootLoop
                        }
                    }

                    if (next.computedEndDate.isBeforeOrEqualTo(event.startDate)) {
                        line.add(event)
                        return@rootLoop
                    }
                }
            }

            lines.add(mutableListOf(event))
        }

        if (result[null]?.isEmpty() == true) {
            result.remove(null)
        }
        itemsByStage = result
    }

    /**
     * Get the horizontal coordinate of a time within the canvas
     */
    private fun horizontalOffsetOf(time: ZonedDateTime): Int {
        return (getDifference(
            startDate, time
        ) * origin.resources.displayMetrics.widthPixels / minutesToWidthScreenRatio).toInt()
    }

    //region Dates manipulations
    private var minutesToWidthScreenRatio = 0
    private var startDate: ZonedDateTime = ZonedDateTime.now()
    private var endDate: ZonedDateTime = ZonedDateTime.now()

    private fun getDifference(startDate: ZonedDateTime, endDate: ZonedDateTime): Long {
        return Duration.between(startDate, endDate).toMinutes()
    }

    private fun getStartDate(events: List<EventData>): ZonedDateTime {
        return (events.minByOrNull { it.startDate }?.startDate ?: getTimeNow()).minusMinutes(10)
    }

    private fun getEndDate(events: List<EventData>): ZonedDateTime {
        val endDate = events.maxByOrNull { it.computedEndDate }?.computedEndDate ?: getTimeNow().plusMinutes(
            minutesToWidthScreenRatio.toLong()
        )

        val finalDate = endDate.plusMinutes(5)
        return if (getDifference(startDate, finalDate) < minutesToWidthScreenRatio) {
            startDate.plusMinutes(minutesToWidthScreenRatio.toLong())
        } else {
            finalDate
        }
    }

    private fun getTimeNow(): ZonedDateTime {
        return ZonedDateTime.now(timezoneProvider.zoneId)
    }
    //endregion

    private fun resetScroll() {
        val now = getTimeNow()
        (recyclerView?.layoutManager as? TimelineLayoutManager)
            ?.takeIf { !it.tryRestoreScroll() }
            ?.let {
                if (now in startDate..endDate) {
                    val scrollToTime = maxOf(now.minusMinutes((minutesToWidthScreenRatio * 0.33).toLong()), startDate)
                    it.scrollTo(horizontalOffsetOf(scrollToTime), 0)
                } else {
                    it.scrollTo(0, 0)
                }
            }
    }

    internal data class EventData(
        val id: Long,
        val name: String,
        val stageId: Long?,
        val stageLabel: String?,
        val stageOrder: Int?,
        val isInMySchedule: Boolean,
        val startDate: ZonedDateTime,
        val endDate: ZonedDateTime?,
        val computedEndDate: ZonedDateTime,
    )

    /**
     * Mainly used when changes happens in MySchedule, to avoid rerunning [prepareLayout] if it's just one item to update
     */
    private object TimelineDiffUtil : DiffUtil.ItemCallback<EventData>() {
        fun shouldRedrawAll(oldItems: List<EventData>, newItems: List<EventData>): Boolean {
            return if (
                oldItems.isEmpty()
                || newItems.isEmpty()
                || oldItems.size != newItems.size
            ) {
                true
            } else {
                val copyOldItems = oldItems.toMutableList()

                //If all given items are in the current list, then it's considered an update
                !newItems.all { newItem ->
                    copyOldItems.removeIf { oldItem ->
                        areItemsTheSame(oldItem, newItem)
                    }
                }
            }
        }

        override fun areItemsTheSame(oldItem: EventData, newItem: EventData): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EventData, newItem: EventData): Boolean =
            oldItem == newItem
    }

    /**
     * Contains information about a RecyclerView's item
     * @property type [Type] - The type of item
     * @property rect The coordinates that the item's view occupies
     * @property isOffsetEnabledHorizontally Boolean representing if that item will move on an horizontal scroll
     * @property isOffsetEnabledVertically Boolean representing if that item will move on a vertical scroll
     */
    internal class ItemAttributes(
        val type: Type,
        val rect: Rect,
        val isOffsetEnabledHorizontally: Boolean = true,
        val isOffsetEnabledVertically: Boolean = true,
    ) {
        /**
         * Copy a [ItemAttributes] with a new Rect
         */
        fun copy(): ItemAttributes =
            ItemAttributes(type, Rect(rect), isOffsetEnabledHorizontally, isOffsetEnabledVertically)
    }

    /**
     * Represents all types of items that will be added to the RecyclerView
     */
    internal enum class Type {
        //Move with caution, the order of items impact the elevation. The higher the ordinal, the higher the elevation
        BACKGROUND_PANEL,
        HOUR_SEPARATOR,
        TIMELINE_CURSOR,
        EVENT,
        STAGE,
        HOUR_BACKGROUND,
        HOURLINE_CURSOR,
        HOUR,
        ;

        companion object {
            private val values = entries.toTypedArray()
            fun getFromOrdinal(ordinal: Int): Type =
                values[ordinal]
        }
    }

    override fun getItemCount(): Int = itemAttributesList.size()

    override fun getItemId(position: Int): Long {
        return itemAttributesList[position].hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return itemAttributesList.get(position)?.type?.ordinal ?: -1
    }

    operator fun get(position: Int): ItemAttributes? {
        return itemAttributesList.get(position)
    }

    fun isOffsetEnabledHorizontally(position: Int): Boolean {
        return itemAttributesList.get(position)?.isOffsetEnabledHorizontally == true
    }

    fun isOffsetEnabledVertically(position: Int): Boolean {
        return itemAttributesList.get(position)?.isOffsetEnabledVertically == true
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }
}
