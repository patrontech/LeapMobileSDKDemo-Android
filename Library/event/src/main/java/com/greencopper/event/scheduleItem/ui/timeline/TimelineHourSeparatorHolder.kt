package com.greencopper.event.scheduleItem.ui.timeline

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor

internal class TimelineHourSeparatorHolder(context: Context) : RecyclerView.ViewHolder(View(context)) {
    fun setup() {
        itemView.setBackgroundResource(R.drawable.vertical_dotted_line)
        itemView.backgroundTintList = ColorStateList.valueOf(EventColor.schedule.timeline.timeIndicator)
    }
}

