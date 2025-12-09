package com.greencopper.event.scheduleItem.ui.timeline

import android.content.Context
import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor

internal class TimelineHourlineCursorHolder(context: Context) : RecyclerView.ViewHolder(ImageView(context)) {
    fun setup() {
        with(itemView as ImageView) {
            setImageResource(R.drawable.timeline_time_indicator)
            imageTintList = ColorStateList.valueOf(EventColor.schedule.timeline.currentTimeIndicator)
        }
    }
}
