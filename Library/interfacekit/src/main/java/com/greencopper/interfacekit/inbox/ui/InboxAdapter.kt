package com.greencopper.interfacekit.inbox.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.InboxHeaderLayoutBinding
import com.greencopper.interfacekit.databinding.InboxItemLayoutBinding
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setTextOrGone
import com.greencopper.toolkit.extensions.isSameDayAs
import com.greencopper.toolkit.extensions.isYesterdayFor
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class InboxAdapter(
    private val inboxTimezone: ZoneId,
) : ListAdapter<InboxAdapter.InboxItem, RecyclerView.ViewHolder>(InboxAdapterDiffUtil()) {

    private val colors: InterfaceKitColor.Inbox
        get() = InterfaceKitColor.inbox

    private val fonts: InterfaceKitTextStyle.Inbox
        get() = InterfaceKitTextStyle.inbox

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HEADER_VIEW_TYPE -> {
                val binding = InboxHeaderLayoutBinding.inflate(inflater, parent, false)
                InboxHeaderViewHolder(binding)
            }
            ITEM_VIEW_TYPE -> {
                val binding = InboxItemLayoutBinding.inflate(inflater, parent, false)
                InboxNotificationItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is InboxHeaderViewHolder -> holder.bind(getItem(position) as HeaderItem)
        is InboxNotificationItemViewHolder -> holder.bind(getItem(position) as NotificationItem)
        else -> throw IllegalArgumentException("Unknown view holder $holder")
    }

    @InboxViewType
    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HeaderItem -> HEADER_VIEW_TYPE
        is NotificationItem -> ITEM_VIEW_TYPE
        else -> throw IllegalArgumentException("Item ${getItem(position)} is not handled by the adapter.")
    }

    internal inner class InboxHeaderViewHolder(val binding: InboxHeaderLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.inboxHeaderSeparator.setBackgroundColor(colors.header.separator)
            binding.root.setBackgroundColor(colors.header.background)
            with(binding.inboxHeaderLabel) {
                setTextColor(colors.header.text)
                setFont(fonts.header.text)
            }
        }

        fun bind(header: HeaderItem) {
            with(binding.inboxHeaderLabel) {
                text = computeTime(
                    binding.root.context,
                    header.label.withZoneSameInstant(inboxTimezone)
                )
            }
        }

        private fun computeTime(context: Context, time: ZonedDateTime): String {
            val now = ZonedDateTime.now(inboxTimezone)
            val isTodayOrYesterday = now.isSameDayAs(time) || time.isYesterdayFor(now)
            //We display days as Today, Yesterday and the exact date if it's more than 2 days
            return if (isTodayOrYesterday) {
                DateUtils.getRelativeDateTimeString(
                    context,
                    time.toInstant().toEpochMilli(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_WEEKDAY,
                ).split(", ").first().toString()

            } else {
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                time.format(formatter)
            }
        }
    }

    internal inner class InboxNotificationItemViewHolder(val binding: InboxItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.background = ColorDrawable(Color.WHITE).apply {
                setTintList(colors.item.toColorStateList())
            }

            with(binding.inboxItemTitleLabel) {
                setTextColor(colors.item.title)
                setFont(fonts.item.title)
            }
            with(binding.inboxItemTextLabel) {
                setTextColor(colors.item.text)
                setFont(fonts.item.text)
            }
            with(binding.inboxItemTimeLabel) {
                setTextColor(colors.item.date)
                setFont(fonts.item.date)
            }
        }

        fun bind(item: NotificationItem) {
            binding.inboxItemTitleLabel.text = item.title
            binding.inboxItemTextLabel.setTextOrGone(item.text)
            binding.inboxItemTimeLabel.text =
                computeTime(
                    itemView.context,
                    ZonedDateTime.parse(item.time).withZoneSameInstant(inboxTimezone)
                )

            with(binding.inboxItemClickIcon) {
                item.onItemClicked?.let { onTap ->
                    isVisible = true
                    setColorFilter(colors.item.arrow)
                    binding.root.setOnSafeClickListener { onTap(item.id) }
                } ?: run {
                    isVisible = false
                    binding.root.setOnClickListener(null)
                }
            }
        }

        private fun computeTime(context: Context, time: ZonedDateTime): String {
            val now = ZonedDateTime.now(inboxTimezone)
            val itemTime = time.toInstant().toEpochMilli()
            //If the notification is less than 1 hour old, we show the interval in minutes
            //Between one and 4 hours we show the interval as hours
            //Past 4 hours, we show the hour of the day
            return if (now.isSameDayAs(time) && time.isBefore(now.plusHours(1))) {
                DateUtils.getRelativeDateTimeString(
                    context,
                    itemTime,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_TIME
                ).split(", ").first().toString()
            } else if (now.isSameDayAs(time) && time.isBefore(now.plusHours(4))) {
                DateUtils.getRelativeDateTimeString(
                    context,
                    itemTime,
                    DateUtils.HOUR_IN_MILLIS,
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_SHOW_TIME
                ).split(", ").last().toString()

            } else {
                time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            }
        }
    }

    fun isHeader(itemPosition: Int): Boolean = getItem(itemPosition) is HeaderItem

    interface InboxItem

    data class HeaderItem(val label: ZonedDateTime) : InboxItem

    data class NotificationItem(
        val id: String,
        val time: String,
        val title: String,
        val text: String? = null,
        val onItemClicked: ((String) -> Unit)? = null,
    ) : InboxItem

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        HEADER_VIEW_TYPE,
        ITEM_VIEW_TYPE,
    )
    annotation class InboxViewType

    companion object {
        const val HEADER_VIEW_TYPE: Int = 1
        const val ITEM_VIEW_TYPE: Int = 0
    }

}

private class InboxAdapterDiffUtil : DiffUtil.ItemCallback<InboxAdapter.InboxItem>() {

    override fun areContentsTheSame(
        oldItem: InboxAdapter.InboxItem,
        newItem: InboxAdapter.InboxItem,
    ): Boolean {
        return when {
            oldItem is InboxAdapter.NotificationItem && newItem is InboxAdapter.NotificationItem -> oldItem.title == newItem.title && oldItem.text == newItem.text && oldItem.time == newItem.time
            //If header items are the same then we assume the content is the same, the label shouldn't change so the view shouldn't be updated
            oldItem is InboxAdapter.HeaderItem && newItem is InboxAdapter.HeaderItem -> true
            else -> false
        }
    }

    override fun areItemsTheSame(
        oldItem: InboxAdapter.InboxItem,
        newItem: InboxAdapter.InboxItem,
    ): Boolean {
        return when {
            oldItem is InboxAdapter.NotificationItem && newItem is InboxAdapter.NotificationItem -> oldItem.id == newItem.id
            oldItem is InboxAdapter.HeaderItem && newItem is InboxAdapter.HeaderItem -> oldItem.label == newItem.label
            else -> false
        }
    }
}
