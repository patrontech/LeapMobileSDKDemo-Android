package com.greencopper.event.scheduleItem.ui.timeline

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.colors.EventColor

internal class TimelineHourBackgroundHolder(context: Context) : RecyclerView.ViewHolder(View(context)) {
    fun setup() {
        itemView.setBackgroundColor(EventColor.schedule.timeline.hourLine.background)
    }
}
