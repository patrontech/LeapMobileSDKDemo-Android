package com.greencopper.interfacekit.widgets.ui.widgetcollection.integration

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.interfacekit.databinding.WidgetCollectionCellBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.BottomDrawableItemDecorator
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

public class WidgetCollectionCell(
    private val binding: WidgetCollectionCellBinding,
) : JobAwareViewHolder(binding.root) {

    public companion object {
        public const val ADAPTER_TYPE: Int = 999999
    }

    init {
        binding.widgetCollection.isNestedScrollingEnabled = false
    }

    /**
     * In case the WidgetCollectionCell is shown in a RecyclerView that has a separator drawable,
     * we pass emptyStateReplacer so it can be shown if there are no widgets to show because of conditions.
     */
    public fun bind(
        widgetItems: List<WidgetCollectionView.WidgetItem>,
        origin: Layout,
        screenName: String,
        emptyStateReplacer: BottomDrawableItemDecorator.DecoratorInfos? = null,
        isFirst: Boolean,
        isLast: Boolean,
        conditionChecker: ConditionChecker? = null,
    ) {
        jobs.add(origin.viewLifecycleOwner.lifecycleScope.launch {
            origin.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.widgetCollection.bind(
                    widgetItems = widgetItems,
                    origin = origin,
                    screenName = screenName,
                    jobs = jobs,
                    topMarginOverride = if(isFirst) null else 0,
                    bottomMarginOverride = if(isLast) null else 0,
                    conditionChecker = conditionChecker,
                ).collectLatest { isNotEmpty ->
                    updateEmptyState(!isNotEmpty, emptyStateReplacer)
                }
            }
        })
    }

    private fun updateEmptyState(isVisible: Boolean, emptyStateReplacer: BottomDrawableItemDecorator.DecoratorInfos?) {
        if (!isVisible || emptyStateReplacer == null) {
            binding.emptyState.isVisible = false
        } else {
            with(binding.emptyState) {
                setImageDrawable(emptyStateReplacer.drawable)
                updateLayoutParams<FrameLayout.LayoutParams> {
                    if (emptyStateReplacer.fullWidth) {
                        width = MATCH_PARENT
                        emptyStateReplacer.drawableHorizontalPaddingDp?.let {
                            val padding = it.dpToPx()
                            setPadding(padding, 0, padding, 0)
                        }
                    } else {
                        width = WRAP_CONTENT
                        setPadding(0, 0, 0, 0)
                    }
                }
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }
}

@Serializable
public data class WidgetCollectionData(
    val index: Int,
    val collection: WidgetCollectionConfiguration.Instance,
)

public fun WidgetCollectionData.toLayoutData(): WidgetCollectionCellLayoutData =
    WidgetCollectionCellLayoutData(index, collection)

@Serializable
public data class WidgetCollectionCellLayoutData(
    val index: Int,
    val collection: WidgetCollectionConfiguration.Instance,
)

public fun List<WidgetCollectionCellLayoutData>.toWidgetItemsBySortedIndex(widgetResolver: WidgetResolver): LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>> =
    linkedMapOf<Int, List<WidgetCollectionView.WidgetItem>>().apply {
        distinctBy { it.index }
            .sortedBy { it.index }
            .forEach {
                val widgets = it.collection.widgets.toWidgetItems(widgetResolver)
                if (widgets.isNotEmpty()) {
                    put(it.index, widgets)
                }
            }
    }

