package com.greencopper.interfacekit.widgets.ui

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.ViewBinding
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import kotlinx.coroutines.Job

public typealias WidgetParameters = KiboSerializable<*>

public abstract class WidgetLayout<T : KiboSerializable<T>> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), LifecycleObserver {

    init {
        setupWidth()
    }

    public abstract val binding: ViewBinding
    protected abstract val widgetCategory: String

    ////<editor-fold desc="ItemDecoration attributes"> ///////////////////////
    public abstract val verticalMargin: Int
    //</editor-fold>/ END: ItemDecoration attributes ///////////////////////

    @JvmName("bindAbstractType")
    public fun bind(params: WidgetParameters, screenName: String, origin: Layout, jobs: MutableList<Job>) {
        try {
            bind(params as T, screenName, origin, jobs)
        } catch (ex: ClassCastException) {
            throw WidgetException.ParametersCastFailed(params)
        }
    }

    protected abstract fun bind(params: T, screenName: String, origin: Layout, jobs: MutableList<Job>)

    public abstract fun getWidgetItemName(params: T): String?

    protected fun buildAnalytics(
        params: T,
        screenName: String,
    ): MutableMap<EventParameter, String> {
        val analytics: MutableMap<EventParameter, String> = buildWidgetAnalytics(
            widgetCategory,
            getWidgetItemName(params),
            screenName
        )
        insertAdditionalAnalytics(analytics, params)

        return analytics
    }

    protected open fun insertAdditionalAnalytics(map: MutableMap<EventParameter, String>, params: T) {}

    protected open fun setupWidth() {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }
}

