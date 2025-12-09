package com.greencopper.interfacekit.ui.views

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job

public abstract class JobAwareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    public val jobs: MutableList<Job> = mutableListOf()

    public fun cancelAllJobs(): Unit = jobs.forEach { it.cancel() }
}
