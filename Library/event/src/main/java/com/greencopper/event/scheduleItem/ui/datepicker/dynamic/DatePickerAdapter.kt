package com.greencopper.event.scheduleItem.ui.datepicker.dynamic

import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.greencopper.event.colors.EventColor
import com.greencopper.event.scheduleItem.ui.datepicker.DatePickerButton
import com.greencopper.event.scheduleItem.ui.datepicker.DatePickerViewData
import com.greencopper.interfacekit.common.setOnSafeClickListener
import java.time.ZonedDateTime

internal class DatePickerAdapter :
    ListAdapter<DatePickerViewData, DatePickerAdapter.DatePickerViewHolder>(DatePickerViewDataDiffUtil())
{
    private var selectedItemPosition = 0

    private var onDateChangeListener: ((selectedDate: ZonedDateTime, position: Int) -> Unit)? = null
    private var onCurrentListChanged: (() -> Unit)? = null

    class DatePickerViewHolder(internal val datePickerButton: DatePickerButton) :
        RecyclerView.ViewHolder(datePickerButton)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatePickerViewHolder {
        val buttonView = DatePickerButton(parent.context).apply {
            setBackgroundColor(EventColor.schedule.datepicker.background)
        }
        return DatePickerViewHolder(buttonView)
    }

    override fun onBindViewHolder(holder: DatePickerViewHolder, position: Int) {
        val data = getItem(position)
        holder.datePickerButton.setDate(data)
        holder.datePickerButton.isChecked = selectedItemPosition == position

        holder.datePickerButton.setOnSafeClickListener {
            val selectedPosition = holder.bindingAdapterPosition
            selectDate(selectedPosition)
            onDateChangeListener?.invoke(data.fullDate, selectedPosition)
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<DatePickerViewData>,
        currentList: MutableList<DatePickerViewData>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        onCurrentListChanged?.invoke()
    }

    fun setDateChangeListener(onDateChangeListener: (selectedDate: ZonedDateTime, position: Int)-> Unit) {
        this.onDateChangeListener = onDateChangeListener
    }

    fun setDates(newDates: List<DatePickerViewData>) {
        submitList(newDates)
    }

    fun selectDate(position: Int) {
        notifyItemChanged(selectedItemPosition)
        selectedItemPosition = position
        notifyItemChanged(selectedItemPosition)
    }

    fun setOnCurrentListChanged(listener: () -> Unit) {
        onCurrentListChanged = listener
    }
}

private class DatePickerViewDataDiffUtil : DiffUtil.ItemCallback<DatePickerViewData>() {
    override fun areContentsTheSame(oldItem: DatePickerViewData, newItem: DatePickerViewData): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: DatePickerViewData, newItem: DatePickerViewData): Boolean {
        return oldItem.fullDate == newItem.fullDate
    }
}
