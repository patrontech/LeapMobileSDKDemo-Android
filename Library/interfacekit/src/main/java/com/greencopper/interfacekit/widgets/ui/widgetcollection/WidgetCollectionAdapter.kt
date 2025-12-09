package com.greencopper.interfacekit.widgets.ui.widgetcollection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.greencopper.interfacekit.databinding.WidgetCollectionHeaderItemBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.bannerwidget.BannerWidgetLayout
import com.greencopper.interfacekit.widgets.ui.header.WidgetCollectionHeaderView
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView.*
import kotlinx.coroutines.Job

internal class WidgetCollectionAdapter(
    private val widgetResolver: WidgetResolver,
    private val originFragment: Layout,
    private val analyticsScreenName: String,
    private val parentJobs: MutableList<Job>? = null,
) : ListAdapter<WidgetCollectionItem, RecyclerView.ViewHolder>(WidgetCollectionAdapterDiffUtil()) {

    private val widgetsAvailable = widgetResolver.resolveWidgets()

    @Suppress("PrivatePropertyName")
    private val HEADER_VIEW_TYPE: Int = widgetsAvailable.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            HEADER_VIEW_TYPE -> HeaderViewHolder(
                WidgetCollectionHeaderItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> {
                widgetsAvailable.getOrNull(viewType)?.let { widgetKey ->
                    WidgetViewHolder(buildWidgetLayout(widgetKey, parent).binding)
                } ?: throw IllegalArgumentException("Item type $viewType is not handled by the adapter.")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        getItemId(position)
        when {
            item is HeaderItem && holder is HeaderViewHolder -> {
                holder.bind(item)
            }

            item is WidgetItem && holder is WidgetViewHolder -> {
                holder.bind(
                    item,
                    originFragment,
                    analyticsScreenName,
                    parentJobs,
                )
            }

            else -> throw IllegalArgumentException("Item $item is not handled by the adapter.")
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? JobAwareViewHolder)?.cancelAllJobs()
        super.onViewRecycled(holder)
    }

    private fun buildWidgetLayout(key: WidgetKey, parent: ViewGroup): WidgetLayout<*> =
        widgetResolver.resolveLayout(key, parent.context)

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        is WidgetItem -> {
            widgetsAvailable.indexOf(item.key).takeIf { it > -1 }
                ?: throw IllegalArgumentException("Item $item is not handled by the adapter.")
        }
        is HeaderItem -> HEADER_VIEW_TYPE
        else -> throw IllegalArgumentException("Item $item is not handled by the adapter.")
    }

    class HeaderViewHolder(viewBinding: WidgetCollectionHeaderItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(headerItem: HeaderItem) {
            val headerView = itemView as WidgetCollectionHeaderView
            headerView.setup(headerItem.info)
        }
    }

    class WidgetViewHolder(viewBinding: ViewBinding) : JobAwareViewHolder(viewBinding.root) {
        fun bind(item: WidgetItem, origin: Layout, screenName: String, parentJobs: MutableList<Job>?) {
            val widgetView = itemView as WidgetLayout<*>
            widgetView.bind(
                item.params,
                screenName,
                origin,
                parentJobs ?: jobs,
            )
        }
    }

    private class WidgetCollectionAdapterDiffUtil : DiffUtil.ItemCallback<WidgetCollectionItem>() {

        override fun areContentsTheSame(oldItem: WidgetCollectionItem, newItem: WidgetCollectionItem): Boolean {
            return when {
                oldItem is WidgetItem && newItem is WidgetItem -> oldItem.params.encodeToString() == newItem.params.encodeToString()
                //If header imageName is the same then we assume the content is the same, other attributes shouldn't change
                oldItem is HeaderItem && newItem is HeaderItem -> true
                else -> false
            }
        }

        override fun areItemsTheSame(oldItem: WidgetCollectionItem, newItem: WidgetCollectionItem): Boolean {
            return when {
                oldItem is WidgetItem && newItem is WidgetItem -> oldItem.key == newItem.key
                oldItem is HeaderItem && newItem is HeaderItem -> oldItem.info.imageName == newItem.info.imageName
                else -> false
            }
        }
    }
}
