package com.greencopper.event.scheduleItem.ui.timeline

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class TimelineBackgroundPanelHolder(context: Context) : RecyclerView.ViewHolder(View(context)) {
    fun bind(color: Int) {
        itemView.setBackgroundColor(color)
    }
}
