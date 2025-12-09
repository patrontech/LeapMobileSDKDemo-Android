package com.greencopper.event.scheduleItem.ui.schedule

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.greencopper.core.asset.recipe.Asset.Format.Name.THUMBNAIL
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.ScheduleItemCellBinding
import com.greencopper.event.databinding.ScheduleNextDateButtonBinding
import com.greencopper.event.databinding.ScheduleSectionDayHeaderBinding
import com.greencopper.event.databinding.ScheduleSectionTimeHeaderBinding
import com.greencopper.event.scheduleItem.viewmodel.ScheduleListViewData
import com.greencopper.event.scheduleItem.viewmodel.ScheduleListViewData.HeaderItem.DayHeaderItem
import com.greencopper.event.scheduleItem.viewmodel.ScheduleListViewData.HeaderItem.TimeHeaderItem
import com.greencopper.event.scheduleItem.viewmodel.ScheduleListViewData.NextDateButton
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.WidgetCollectionCellBinding
import com.greencopper.interfacekit.favorites.FavoriteIcons
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.resetImageFrom
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.setShadowColor
import com.greencopper.interfacekit.ui.setTextOrGone
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCell
import kotlinx.coroutines.CoroutineScope
import java.time.ZonedDateTime

internal class ScheduleListAdapter(
    private val onScheduleItemClicked: (Long) -> Unit,
    private val onAddRemoveMyScheduleItemClicked: (ScheduleListViewData.ScheduleItem) -> Unit,
    private val onNextDateButtonTap: (ZonedDateTime) -> Unit,
    private val lifecycleScope: CoroutineScope,
    private val favoriteIcons: FavoriteIcons? = null,
    private val displayImages: Boolean,
    private val origin: Layout,
    private val screenName: String,
    private val conditionChecker: ConditionChecker,
) : ListAdapter<ScheduleListViewData, JobAwareViewHolder>(ScheduleAdapterDiffUtil) {

    companion object {
        internal const val VIEW_TYPE_TIME_HEADER = 1
        internal const val VIEW_TYPE_DAY_HEADER = 2
        internal const val VIEW_TYPE_CARD = 3
        internal const val VIEW_TYPE_WIDGETS = 4
        internal const val VIEW_TYPE_NEXT_DATE = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimeHeaderItem -> VIEW_TYPE_TIME_HEADER
            is DayHeaderItem -> VIEW_TYPE_DAY_HEADER
            is ScheduleListViewData.ScheduleItem -> VIEW_TYPE_CARD
            is ScheduleListViewData.WidgetCollectionHolder -> VIEW_TYPE_WIDGETS
            is NextDateButton -> VIEW_TYPE_NEXT_DATE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobAwareViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_TIME_HEADER -> {
                val timeHeaderView = ScheduleSectionTimeHeaderBinding.inflate(inflater, parent, false)
                timeHeaderView.setStyles()
                TimeHeaderViewHolder(timeHeaderView)
            }

            VIEW_TYPE_DAY_HEADER -> {
                val headerView = ScheduleSectionDayHeaderBinding.inflate(inflater, parent, false)
                headerView.setStyles()
                DayHeaderViewHolder(headerView)
            }

            VIEW_TYPE_CARD -> {
                val cardView = ScheduleItemCellBinding.inflate(inflater, parent, false)
                cardView.setStyles()
                val bounceAnimation = AnimatorInflater.loadAnimator(origin.context, R.animator.bounce_scale)
                ScheduleCardViewHolder(cardView, bounceAnimation)
            }

            VIEW_TYPE_WIDGETS -> {
                val widgetsView = WidgetCollectionCellBinding.inflate(inflater, parent, false)
                WidgetCollectionCell(widgetsView)
            }

            VIEW_TYPE_NEXT_DATE -> {
                val nextDateView = ScheduleNextDateButtonBinding.inflate(inflater, parent, false)
                nextDateView.setStyles()
                NextDateButtonViewHolder(nextDateView)
            }

            else -> throw IllegalStateException("${ScheduleListAdapter::class.simpleName} not set up properly")
        }
    }

    override fun onViewRecycled(holder: JobAwareViewHolder) {
        holder.cancelAllJobs()
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: JobAwareViewHolder, position: Int) {
        when (holder) {
            is TimeHeaderViewHolder -> holder.bind(getItem(position) as TimeHeaderItem)
            is DayHeaderViewHolder -> holder.bind(getItem(position) as DayHeaderItem)
            is NextDateButtonViewHolder -> holder.bind(getItem(position) as NextDateButton)
            is ScheduleCardViewHolder ->
                holder.bind(
                    getItem(position) as ScheduleListViewData.ScheduleItem,
                )

            is WidgetCollectionCell ->
                holder.bind(
                    (getItem(position) as ScheduleListViewData.WidgetCollectionHolder).widgets,
                    origin,
                    screenName,
                    null,
                    position == 0,
                    position == itemCount - 1,
                    conditionChecker = conditionChecker,
                )

            else -> throw IllegalStateException("${ScheduleListAdapter::class.simpleName} not set up properly")
        }
    }

    fun setViewData(newRecyclerItems: List<ScheduleListViewData>, applyIfContentChanged: () -> Unit) {
        val isContentUpdate = ScheduleAdapterDiffUtil.isContentUpdate(currentList, newRecyclerItems)
        submitList(newRecyclerItems) {
            if (isContentUpdate) applyIfContentChanged()
        }
    }

    private inner class ScheduleCardViewHolder(
        private val scheduleCardBinding: ScheduleItemCellBinding,
        private val bounceAnimation: Animator,
    ) : JobAwareViewHolder(scheduleCardBinding.root) {

        fun bind(
            scheduleListItem: ScheduleListViewData.ScheduleItem,
        ) {
            with(scheduleCardBinding) {
                with(scheduleItemLayout) {
                    cardView.isVisible = displayImages
                    tvItemName.text = scheduleListItem.name
                    tvItemTime.setTextOrGone(scheduleListItem.timeOfEvent)
                    tvItemStage.setTextOrGone(scheduleListItem.stageLabel)
                    tvItemTime.contentDescription =
                        scheduleListItem.timeOfEvent?.replaceTimeForAccessibility()

                    ivItemImage.resetImageFrom(
                        scheduleListItem.photo,
                        lifecycleScope,
                        format = THUMBNAIL,
                    )?.also { jobs.add(it) }

                    with(ivItemAddRemoveMySchedule) {
                        bounceAnimation.setTarget(ivItemAddRemoveMySchedule)
                        isVisible = favoriteIcons != null
                        setOnSafeClickListener {
                            bounceAnimation.start()
                            onAddRemoveMyScheduleItemClicked(scheduleListItem)
                        }

                        val imageName = if (scheduleListItem.isInMySchedule) {
                            contentDescription = favoriteIcons?.removeAccessibilityLabel
                            favoriteIcons?.removeIcon
                        } else {
                            contentDescription = favoriteIcons?.addAccessibilityLabel
                            favoriteIcons?.addIcon
                        }

                        imageName?.let {
                            jobs.add(
                                setImageFrom(
                                    it, lifecycleScope,
                                    hideIfUnknown = true,
                                    hideIfLoading = true,
                                )
                            )
                        } ?: setImageDrawable(null)
                    }
                }

                scheduleItemCard.setOnSafeClickListener {
                    onScheduleItemClicked(scheduleListItem.itemId)
                }
            }
        }
    }

    private fun String.replaceTimeForAccessibility() = replace(Regex("([0-9]{1,2}):([0-9]{1,2})")) {
        val minutes = if (it.groupValues.lastOrNull() == "00") {
            "0"
        } else {
            it.groupValues.lastOrNull()
        }
        "${it.groupValues[1]} $minutes"
    }

    private inner class TimeHeaderViewHolder(
        private val headerBinding: ScheduleSectionTimeHeaderBinding,
    ) : JobAwareViewHolder(headerBinding.root) {

        fun bind(recyclerItem: ScheduleListViewData) {
            val timeHeaderItem = recyclerItem as TimeHeaderItem
            headerBinding.time.text = timeHeaderItem.startTime
            headerBinding.time.contentDescription = timeHeaderItem.startTime.replaceTimeForAccessibility()
        }
    }

    private inner class DayHeaderViewHolder(
        private val headerBinding: ScheduleSectionDayHeaderBinding,
    ) : JobAwareViewHolder(headerBinding.root) {

        fun bind(recyclerItem: ScheduleListViewData) {
            val dayHeaderItem = recyclerItem as DayHeaderItem
            headerBinding.day.text = dayHeaderItem.day
            headerBinding.day.contentDescription = dayHeaderItem.day.replaceTimeForAccessibility()
        }
    }

    private inner class NextDateButtonViewHolder(
        private val nextDateButtonBinding: ScheduleNextDateButtonBinding,
    ) : JobAwareViewHolder(nextDateButtonBinding.root) {
        fun bind(recyclerItem: ScheduleListViewData) {
            val nextDateItem = recyclerItem as NextDateButton
            nextDateButtonBinding.button.text = nextDateItem.label
            nextDateButtonBinding.button.setOnSafeClickListener { onNextDateButtonTap(nextDateItem.nextDate) }
        }
    }
}

private fun ScheduleSectionTimeHeaderBinding.setStyles() {
    val colors = EventColor.schedule.timeSeparator
    time.setTextColor(colors.label)
    time.setFont(EventTextStyle.schedule.list.timeSeparator.label)
    bullet.background.setTint(colors.leftShape)
    dash.background.setTint(colors.dash)
}

private fun ScheduleSectionDayHeaderBinding.setStyles() {
    day.setTextColor(EventColor.schedule.daySeparator)
    day.setFont(EventTextStyle.schedule.list.daySeparator.label)
}

private fun ScheduleItemCellBinding.setStyles() {
    val colors = EventColor.schedule.card
    val textStyles = EventTextStyle.schedule.list.card
    scheduleItemCard.backgroundTintList = colors.background.toColorStateList()
    scheduleItemCard.strokeColor = colors.border
    scheduleItemCard.setShadowColor(colors.shadow)

    with(scheduleItemLayout) {

        cardView.strokeColor = colors.image.stroke
        tvItemName.setTextColor(colors.name)
        tvItemName.setFont(textStyles.name)
        tvItemStage.setTextColor(colors.stage)
        tvItemStage.setFont(textStyles.stage)
        tvItemTime.setTextColor(colors.hours)
        tvItemTime.setFont(textStyles.hours)
        ivItemAddRemoveMySchedule.setColorFilter(colors.mySchedule.normal)
    }
}

private fun ScheduleNextDateButtonBinding.setStyles() {
    val colors = EventColor.schedule.nextButton

    with(button) {
        strokeColor = ColorStateList.valueOf(colors.border)
        setTextColor(colors.text)
        background.setTint(colors.background)
        setFont(EventTextStyle.schedule.list.nextButton.label)
    }
}

private object ScheduleAdapterDiffUtil : DiffUtil.ItemCallback<ScheduleListViewData>() {

    fun isContentUpdate(oldItems: List<ScheduleListViewData>, newItems: List<ScheduleListViewData>): Boolean {
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

    override fun areContentsTheSame(
        oldItem: ScheduleListViewData,
        newItem: ScheduleListViewData,
    ): Boolean {
        return when {
            oldItem is ScheduleListViewData.HeaderItem && newItem is ScheduleListViewData.HeaderItem ->
                true

            oldItem is ScheduleListViewData.ScheduleItem && newItem is ScheduleListViewData.ScheduleItem ->
                oldItem == newItem

            oldItem is ScheduleListViewData.WidgetCollectionHolder && newItem is ScheduleListViewData.WidgetCollectionHolder ->
                oldItem.widgets == newItem.widgets

            oldItem is NextDateButton && newItem is NextDateButton ->
                oldItem == newItem

            else -> false
        }
    }

    override fun areItemsTheSame(
        oldItem: ScheduleListViewData,
        newItem: ScheduleListViewData,
    ): Boolean {
        return when {
            oldItem is ScheduleListViewData.HeaderItem && newItem is ScheduleListViewData.HeaderItem ->
                oldItem.label == newItem.label

            oldItem is ScheduleListViewData.ScheduleItem && newItem is ScheduleListViewData.ScheduleItem ->
                oldItem.itemId == newItem.itemId

            oldItem is ScheduleListViewData.WidgetCollectionHolder && newItem is ScheduleListViewData.WidgetCollectionHolder ->
                oldItem.key == newItem.key

            oldItem is NextDateButton && newItem is NextDateButton ->
                oldItem == newItem

            else -> false
        }
    }

}
