package com.greencopper.event.activity.ui.activitydetail

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.*
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.ScheduleItemCardviewBinding
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.event.scheduleItem.ui.bind
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.setShadowColor
import kotlinx.coroutines.CoroutineScope

internal class ScheduleItemCardListAdapter(
    context: Context,
    private val lifecycleScope: CoroutineScope,
    private val origin: Layout,
    private val myScheduleEditingInfo: MyScheduleEditingInfo?,
    private val stageDetailIcon: String,
    private val onScheduleItemTap: ((ScheduleItemViewData) -> Unit)?,
) : ListAdapter<ScheduleItemAdapterData, ScheduleItemCardListAdapter.ScheduleItemViewHolder>(ScheduleCardAdapterDiffUtil()) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var itemHeight: Int = 0
    private var itemWidth: Int = 288.dpToPx()

    inner class ScheduleItemViewHolder(
        private val binding: ScheduleItemCardviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        internal fun bind(data: ScheduleItemAdapterData) {
            binding.scheduleItemItemview.bind(
                data.scheduleItemData,
                origin,
                lifecycleScope,
                myScheduleEditingInfo,
                stageDetailIcon = stageDetailIcon,
                timeVisible = data.showTime,
            )

            with(binding.scheduleItemCardviewTitle) {
                text = data.scheduleItemData.name
                isVisible = data.showTitle
            }

            if (onScheduleItemTap != null) {
                binding.scheduleItemCard.setOnSafeClickListener {
                    onScheduleItemTap.invoke(data.scheduleItemData)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleItemViewHolder {
        val scheduleColors = EventColor.activityDetail.upcomingTimes.schedule
        val scheduleTextStyles = EventTextStyle.activityDetail.upcomingTimes.schedule
        val cardColors = EventColor.activityDetail.upcomingTimes.card

        val itemView = ScheduleItemCardviewBinding.inflate(inflater, parent, false)
        with(itemView.scheduleItemCard) {
            backgroundTintList = cardColors.background.toColorStateList()
            strokeColor = cardColors.border
            setShadowColor(cardColors.shadow)
        }

        with(itemView.scheduleItemCardviewTitle) {
            setTextColor(scheduleColors.title)
            setFont(scheduleTextStyles.title)
        }

        with(itemView.scheduleItemItemview) {
            scheduleItemTvDayOfEvent.setTextColor(scheduleColors.date.day)
            scheduleItemTvDayOfEvent.setFont(scheduleTextStyles.day)

            scheduleItemTvTimeOfEvent.setTextColor(scheduleColors.date.hours)
            scheduleItemTvTimeOfEvent.setFont(scheduleTextStyles.hours)

            scheduleItemStage.stageTv.setTextColor(scheduleColors.stage.name)
            scheduleItemStage.stageTv.setFont(scheduleTextStyles.stage)

            scheduleItemIv.setColorFilter(scheduleColors.date.icon)
            scheduleItemStage.stageIv.setColorFilter(scheduleColors.stage.icon)
            scheduleItemStage.stageMapPin.setColorFilter(scheduleColors.stage.mapPin)
            scheduleItemAddRemove.setColorFilter(scheduleColors.mySchedule.selected)
        }

        itemView.root.updateLayoutParams<RecyclerView.LayoutParams> {
            width = itemWidth
            height = itemHeight
        }

        return ScheduleItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScheduleItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal fun setScheduleItems(newScheduleItems: List<ScheduleItemViewData>, showTitle: Boolean) {
        val showTime = newScheduleItems.any { it.timeOfEvent != null }
        submitList(newScheduleItems.map { ScheduleItemAdapterData(it, showTime, showTitle) })

        measureItemSize()
    }

    private fun measureItemSize() {
        val viewHolder = ScheduleItemViewHolder(ScheduleItemCardviewBinding.inflate(inflater))

        itemWidth = when (itemCount) {
            1 -> Resources.getSystem().displayMetrics.widthPixels - 48.dpToPx()
            else -> 288.dpToPx()
        }

        for (i in 0 until itemCount) {
            val item = getItem(i)
            viewHolder.bind(item)

            viewHolder.itemView.measure(
                MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            )

            itemHeight = maxOf(itemHeight, viewHolder.itemView.measuredHeight)
        }
    }
}

private class ScheduleCardAdapterDiffUtil : DiffUtil.ItemCallback<ScheduleItemAdapterData>() {
    override fun areContentsTheSame(oldItem: ScheduleItemAdapterData, newItem: ScheduleItemAdapterData): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: ScheduleItemAdapterData, newItem: ScheduleItemAdapterData): Boolean {
        return oldItem.scheduleItemData.itemId == newItem.scheduleItemData.itemId
    }
}

internal data class ScheduleItemAdapterData(
    val scheduleItemData: ScheduleItemViewData,
    val showTime: Boolean,
    val showTitle: Boolean,
)
