package com.greencopper.interfacekit.filtering.filterselector.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.*
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.filtering.FilterId
import com.greencopper.interfacekit.filtering.filterselector.FilterSelectorData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve

internal class FilterSelectorAdapter :
    ListAdapter<FilterSelectorRecyclerViewData, FilterSelectorAdapter.BaseViewHolder>(
        FilterSelectorAdapterDiffUtil()
    ) {

    private enum class ViewType {
        Header, Option
    }

    internal sealed class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        protected val localizationService: LocalizationService by lazy { App.resolve() }

        internal abstract fun bind(recyclerItem: FilterSelectorRecyclerViewData)

        class OptionViewHolder(
            private val filterOptionView: FilterOptionRecyclerItemView
        ) : BaseViewHolder(filterOptionView) {
            override fun bind(recyclerItem: FilterSelectorRecyclerViewData) {
                val data = recyclerItem as FilterSelectorRecyclerViewData.OptionItemViewData
                filterOptionView.setTitle(localizationService.getString(data.title))
                filterOptionView.isChecked = data.isChecked
                filterOptionView.setOnSafeClickListener(100) {
                    data.onTap()
                }
            }
        }

        class HeaderViewHolder(
            private val titleView: TextView
        ) : BaseViewHolder(titleView) {

            override fun bind(recyclerItem: FilterSelectorRecyclerViewData) {
                titleView.text = localizationService.getString(recyclerItem.title)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (ViewType.values().first { it.ordinal == viewType }) {
            ViewType.Header -> {
                val headerView = TextView(parent.context)
                headerView.setTextColor(InterfaceKitColor.filters.title)
                headerView.setFont(InterfaceKitTextStyle.filterSelector.title)
                BaseViewHolder.HeaderViewHolder(headerView)
            }
            ViewType.Option -> {
                val optionView = FilterOptionRecyclerItemView(parent.context)
                BaseViewHolder.OptionViewHolder(optionView)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FilterSelectorRecyclerViewData.HeaderViewData -> ViewType.Header.ordinal
            is FilterSelectorRecyclerViewData.OptionItemViewData -> ViewType.Option.ordinal
        }
    }

    fun setDataFilters(ids: List<FilterId>, filters: Map<FilterId, List<FilterSelectorData.Cell>>) {
        val map = ids.flatMap {
            filters[it] ?: emptyList()
        }.map {
            when (it) {
                is FilterSelectorData.Cell.Title -> FilterSelectorRecyclerViewData.HeaderViewData(it.title)
                is FilterSelectorData.Cell.Option -> FilterSelectorRecyclerViewData.OptionItemViewData(
                    it.label,
                    it.isActive,
                    it.onTap
                )
            }
        }
        submitList(map)
    }
}

internal sealed class FilterSelectorRecyclerViewData {

    abstract val title: String

    data class OptionItemViewData(
        override val title: String,
        val isChecked: Boolean = false,
        val onTap: () -> Unit
    ) : FilterSelectorRecyclerViewData()

    data class HeaderViewData(
        override val title: String
    ) : FilterSelectorRecyclerViewData()
}

private class FilterSelectorAdapterDiffUtil :
    DiffUtil.ItemCallback<FilterSelectorRecyclerViewData>() {
    override fun areItemsTheSame(
        oldItem: FilterSelectorRecyclerViewData,
        newItem: FilterSelectorRecyclerViewData
    ): Boolean =
        oldItem::class == newItem::class
                && oldItem.title == newItem.title

    override fun areContentsTheSame(
        oldItem: FilterSelectorRecyclerViewData,
        newItem: FilterSelectorRecyclerViewData
    ): Boolean =
        oldItem::class == newItem::class
                && oldItem.title == newItem.title
                && (oldItem as? FilterSelectorRecyclerViewData.OptionItemViewData)?.isChecked == ((newItem as? FilterSelectorRecyclerViewData.OptionItemViewData)?.isChecked)
}
