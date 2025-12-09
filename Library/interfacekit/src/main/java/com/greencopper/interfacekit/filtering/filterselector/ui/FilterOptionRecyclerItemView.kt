package com.greencopper.interfacekit.filtering.filterselector.ui

import android.content.Context
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Checkable
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.FilterOptionRecyclerItemBinding
import com.greencopper.interfacekit.metrics.filterOptionTap
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont

internal class FilterOptionRecyclerItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs), Checkable {

    private companion object {
        private val checkedStateSet = intArrayOf(android.R.attr.state_checked)
    }

    private val binding =
        FilterOptionRecyclerItemBinding.inflate(LayoutInflater.from(context), this)

    init {
        val colors = InterfaceKitColor.filters.checkBox
        binding.filterItemTv.setTextColor(colors.name)
        binding.filterItemTv.setFont(InterfaceKitTextStyle.filterSelector.checkBox.name)

        val uncheckedDrawable =
            ContextCompat.getDrawable(context, R.drawable.filter_button_item_unchecked)
        val checkedDrawable =
            ContextCompat.getDrawable(context, R.drawable.filter_button_item_checked)

        val checkBoxDrawable = StateListDrawable()
        checkBoxDrawable.addState(intArrayOf(android.R.attr.state_checked), checkedDrawable)
        checkBoxDrawable.addState(intArrayOf(-android.R.attr.state_checked), uncheckedDrawable)
        binding.filterItemCb.buttonDrawable = checkBoxDrawable
        binding.filterItemCb.buttonTintList = colors.box.toColorStateList()
    }

    fun setTitle(title: String) {
        binding.filterItemTv.text = title
    }

    private var isChecked = false

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        binding.filterItemCb.isChecked = checked
        binding.filterItemCb.refreshDrawableState()
        refreshDrawableState()
    }

    override fun isChecked(): Boolean = isChecked

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val intArray = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            mergeDrawableStates(intArray, checkedStateSet)
        }
        return intArray
    }
}

internal data class FilterOptionTapEventAnalytics(
    val optionLabel: String,
    val filterLabel: String,
    val filterId: String,
    val screenName: String?
) : MappedMetrics {

    private val parameters: Map<EventParameter, String> = mutableMapOf(
        EventParameter.itemName to optionLabel,
        EventParameter.itemCategory to filterLabel,
        EventParameter.itemId to filterId
    ).apply {
        screenName?.let {
            put(EventParameter.screenName, screenName)
        }
    }

    override fun track(provider: MappedProvider) {
        provider.track(EventName.filterOptionTap(), parameters)
    }
}
