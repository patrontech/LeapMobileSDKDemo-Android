package com.greencopper.event.scheduleItem.ui.timeline

import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.TimelineStageViewBinding
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.setShadowColor

internal class TimelineStageHolder(val binding: TimelineStageViewBinding) : RecyclerView.ViewHolder(binding.root) {
    private val colors get() = EventColor.schedule.timeline.stage

    fun setup(title: String) {
        with(binding.timelineStageLabel) {
            text = title
            setTextColor(colors.title)
            setFont(EventTextStyle.schedule.timeline.stage.title)
            setPadding(24f.dpToPx(), 0, 0, 0)
        }
        binding.root.background = createBackground(colors.background)
        binding.root.elevation = 2f.dpToPx().toFloat()
        binding.root.setShadowColor(colors.shadow)
    }

    private fun createBackground(backgroundColor: Int) =
        GradientDrawable().apply {
            setColor(backgroundColor)
        }
}
