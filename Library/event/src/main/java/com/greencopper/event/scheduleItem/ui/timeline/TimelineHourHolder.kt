package com.greencopper.event.scheduleItem.ui.timeline

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.TimelineHourViewBinding
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont

internal class TimelineHourHolder(private val binding: TimelineHourViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setup() {
        itemView.layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        with(binding.hourTitle) {
            setTextColor(EventColor.schedule.timeline.hourLine.label)
            setFont(EventTextStyle.schedule.timeline.hourLine.label)
            gravity = Gravity.CENTER
        }
    }

    fun bind(text: String) {
        with(binding.hourTitle) {
            setText(text)
        }
    }
}
