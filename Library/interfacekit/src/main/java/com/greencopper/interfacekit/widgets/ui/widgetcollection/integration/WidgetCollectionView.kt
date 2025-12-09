package com.greencopper.interfacekit.widgets.ui.widgetcollection.integration

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.Conditioned
import com.greencopper.core.conditions.authorizedFlow
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.WidgetCollectionItemDecoration
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.ui.widgetcollection.WidgetCollectionAdapter
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.w
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable

public class WidgetCollectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private val widgetResolver: WidgetResolver by App.lazy()

    private lateinit var widgetAdapter: WidgetCollectionAdapter

    private var isSetup = false

    init {
        itemAnimator = null
        clipChildren = false
        clipToPadding = false
    }

    /**
     * /!\ Must collect to execute
     * @return A Flow representing whether the WidgetCollectionView contains widgets after checking conditions.
     * True means the WidgetCollectionView contains something.
     */
    public fun bind(
        widgetHeader: HeaderItem? = null,
        widgetItems: List<WidgetItem>,
        origin: Layout,
        screenName: String,
        conditionChecker: ConditionChecker? = null,
        topMarginOverride: Int? = null,
        bottomMarginOverride: Int? = null,
        jobs: MutableList<Job>? = null,
    ): Flow<Boolean> = channelFlow {
        if (!isSetup) {
            setup(topMarginOverride, bottomMarginOverride)
            widgetAdapter = WidgetCollectionAdapter(widgetResolver, origin, screenName, jobs)
            this@WidgetCollectionView.adapter = widgetAdapter
            isSetup = true
        }

        widgetItems.authorizedFlow(conditionChecker ?: App.resolve())
            .flowOn(Dispatchers.IO)
            .collectLatest { filteredWidgets ->
                updateUI(widgetAdapter, widgetHeader, filteredWidgets)
                send(filteredWidgets.isNotEmpty() || widgetHeader != null)
            }
    }

    private fun setup(topMarginOverride: Int? = null, bottomMarginOverride: Int? = null,) {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        addItemDecoration(WidgetCollectionItemDecoration(topMarginOverride, bottomMarginOverride))
        setItemViewCacheSize(10)
    }

    private fun updateUI(
        adapter: WidgetCollectionAdapter,
        widgetHeader: HeaderItem? = null,
        widgetItems: List<WidgetItem>,
    ) {
        val widgetsWithHeader = ArrayList<WidgetCollectionItem>()
        widgetHeader?.let {
            widgetsWithHeader.add(it)
        }
        widgetsWithHeader.addAll(widgetItems)

        adapter.submitList(widgetsWithHeader)

        this.isVisible = widgetsWithHeader.isNotEmpty()
    }

    public interface WidgetCollectionItem

    public data class HeaderItem(val info: WidgetCollectionConfiguration.Instance.HeaderInfo) : WidgetCollectionItem

    @Serializable
    public data class WidgetItem(
        val key: WidgetCollectionConfiguration.Instance.WidgetKey,
        val params: WidgetParameters,
        override val conditionSet: ConditionSet? = null,
    ) : WidgetCollectionItem, Conditioned
}

public fun List<WidgetCollectionConfiguration.Instance.WidgetInfo>.toWidgetItems(widgetResolver: WidgetResolver): List<WidgetCollectionView.WidgetItem> {
    return mapNotNull {
        try {
            WidgetCollectionView.WidgetItem(
                it.key,
                widgetResolver.resolveParams(it),
                it.conditionSet
            )
        } catch (throwable: Throwable) {
            App.log.w("Problem parsing widget $it", throwable = throwable)
            null
        }
    }
}
