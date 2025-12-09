package com.greencopper.event.scheduleItem.ui.timeline

import android.graphics.Color
import android.graphics.drawable.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.TimelineEventViewBinding
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.PressableColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.toolkit.extensions.getFormattedDateTime
import kotlinx.coroutines.CoroutineScope
import java.time.format.FormatStyle

internal class TimelineEventHolder(
    val binding: TimelineEventViewBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private val normalCardColors get() = EventColor.schedule.timeline.card
    private val myScheduleCardColors get() = EventColor.schedule.timeline.myScheduleCard

    private val normalTextStyles get() = EventTextStyle.schedule.timeline.card
    private val myScheduleTextStyles get() = EventTextStyle.schedule.timeline.myScheduleCard

    private val myScheduleTypeface by lazy { myScheduleTextStyles.title.typeface }
    private val normalTypeface by lazy { normalTextStyles.title.typeface }
    private val myScheduleTitleColor by lazy { myScheduleCardColors.title }
    private val normalTitleColor by lazy { normalCardColors.title }

    private val subtitleMyScheduleTypeface by lazy { myScheduleTextStyles.subtitle.typeface }
    private val subtitleNormalTypeface by lazy { normalTextStyles.subtitle.typeface }
    private val subtitleMyScheduleColor by lazy { myScheduleCardColors.subtitle }
    private val subtitleNormalColor by lazy { normalCardColors.subtitle }

    internal fun minimalSetup(
        item: TimelineAdapter.EventData,
        timezoneProvider: TimezoneProvider,
    ) {
        with(binding.timelineItemLabel) {
            text = item.name
            if (item.isInMySchedule) {
                setTextColor(myScheduleTitleColor)
                typeface = myScheduleTypeface
                textSize = myScheduleTextStyles.title.fontSize
            } else {
                setTextColor(normalTitleColor)
                typeface = normalTypeface
                textSize = normalTextStyles.title.fontSize
            }
        }

        with(binding.timelineItemSubtitle) {
            val subtitle = item.startDate.getFormattedDateTime(
                null,
                FormatStyle.SHORT,
                timezoneProvider.zoneId
            ).plus(
                item.endDate?.let {
                    " - ${
                        it.getFormattedDateTime(
                            null,
                            FormatStyle.SHORT,
                            timezoneProvider.zoneId
                        )
                    }"
                } ?: ""
            )
            text = subtitle
            if (item.isInMySchedule) {
                setTextColor(subtitleMyScheduleColor)
                typeface = subtitleMyScheduleTypeface
                textSize = myScheduleTextStyles.subtitle.fontSize
            } else {
                setTextColor(subtitleNormalColor)
                typeface = subtitleNormalTypeface
                textSize = normalTextStyles.subtitle.fontSize
            }
        }
    }

    internal fun setup(
        item: TimelineAdapter.EventData,
        favoritesEditing: FavoritesEditing? = null,
        lifecycleScope: CoroutineScope,
        timezoneProvider: TimezoneProvider,
        onItemClick: (TimelineAdapter.EventData) -> Unit,
        onAddRemoveMyScheduleItemClicked: (TimelineAdapter.EventData) -> Unit,
    ) {
        minimalSetup(item, timezoneProvider)

        with(binding.timelineItemMyScheduleButton) {
            if (favoritesEditing == null) {
                isVisible = false
            } else {
                isVisible = true
                setOnSafeClickListener { onAddRemoveMyScheduleItemClicked(item) }

                val imageName = if (item.isInMySchedule) {
                    contentDescription = favoritesEditing.remove.accessibilityLabel
                    favoritesEditing.remove.icon
                } else {
                    contentDescription = favoritesEditing.add.accessibilityLabel
                    favoritesEditing.add.icon
                }
                setImageFrom(
                    imageName,
                    lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )

                if (item.isInMySchedule)
                    setColorFilter(myScheduleCardColors.myScheduleIcon)
                else
                    setColorFilter(normalCardColors.myScheduleIcon)
            }
        }

        binding.root.background = if (item.isInMySchedule) {
            createBackground(
                myScheduleCardColors.background,
                myScheduleCardColors.border,
                item.endDate != null
            )
        } else {
            createBackground(
                normalCardColors.background,
                normalCardColors.border,
                item.endDate != null
            )
        }

        binding.root.setOnSafeClickListener { onItemClick(item) }
    }

    private fun createBackground(
        backgroundColor: PressableColor,
        borderColor: Int,
        hasEndTime: Boolean,
    ): Drawable {
        return if (!hasEndTime) {

            val normalDrawable = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.timeline_item_background
            )?.mutate() as LayerDrawable
            (normalDrawable.findDrawableByLayerId(R.id.timelineItemBorder) as GradientDrawable).apply {
                colors = intArrayOf(borderColor, borderColor, Color.TRANSPARENT)
            }
            (normalDrawable.findDrawableByLayerId(R.id.timelineItemBackground) as GradientDrawable).apply {
                colors = intArrayOf(
                    backgroundColor.normal,
                    backgroundColor.normal,
                    Color.TRANSPARENT
                )
            }

            val pressedDrawable = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.timeline_item_background
            )?.mutate() as LayerDrawable
            (pressedDrawable.findDrawableByLayerId(R.id.timelineItemBorder) as GradientDrawable).apply {
                colors = intArrayOf(borderColor, borderColor, Color.TRANSPARENT)
            }
            (pressedDrawable.findDrawableByLayerId(R.id.timelineItemBackground) as GradientDrawable).apply {
                colors = intArrayOf(
                    backgroundColor.pressed,
                    backgroundColor.pressed,
                    Color.TRANSPARENT
                )
            }

            val stateDrawable = StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
                addState(intArrayOf(android.R.attr.state_enabled), normalDrawable)
            }

            stateDrawable
        } else {
            GradientDrawable().apply {
                color = backgroundColor.toColorStateList()
                val strokeWidth = 1f.dpToPx()
                setStroke(strokeWidth, borderColor)
                cornerRadius = binding.root.resources.getDimension(R.dimen.card_corner_radius)
            }.mutate()
        }
    }
}

