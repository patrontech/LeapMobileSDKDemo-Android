package com.greencopper.interfacekit.widgets.ui.titlecounterwidget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.localizationService
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.counter.CounterResolver
import com.greencopper.interfacekit.databinding.TitleCounterWidgetBinding
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.TitleCounterWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.*

internal class TitleCounterWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<TitleCounterWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "title_counter_widget"
    override val binding = TitleCounterWidgetBinding.inflate(LayoutInflater.from(context), this)

    private val localizationService by lazy { App.localizationService() }
    private val linkResolver: LinkResolver by App.lazy()

    override val verticalMargin: Int = 0

    private val counterResolver: CounterResolver by App.lazy()

    override fun getWidgetItemName(params: TitleCounterWidgetParameters): String =
        params.onTap.analytics.itemName

    @Throws(WidgetException::class)
    override fun bind(
        params: TitleCounterWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        val counter = counterResolver.resolve(params.counter.key, params.counter.params)
        val titleCounterWidgetColor = InterfaceKitColor.titleCounterWidget
        val titleCounterWidgetTextStyle = InterfaceKitTextStyle.titleCounterWidget
        with(binding.titleCounterWidgetTitle) {
            text = localizationService.getString(params.title)
            setFont(titleCounterWidgetTextStyle.title)
            setTextColor(titleCounterWidgetColor.title)
        }

        binding.titleCounterWidgetRedirect.imageTintList = ColorStateList.valueOf(titleCounterWidgetColor.chevron)
        binding.titleCounterWidgetSeparator.setBackgroundColor(titleCounterWidgetColor.separator)

        val job = origin.lifecycleScope.launch(Dispatchers.IO) {
            with(binding.titleCounterWidgetNumber) {
                counter?.let {
                    val itemCount = counter.count()
                    withContext(Dispatchers.Main) {
                        setFont(titleCounterWidgetTextStyle.counter)
                        if (itemCount == 0) {
                            setTextColor(titleCounterWidgetColor.counter.empty.label)
                            background.setTint(titleCounterWidgetColor.counter.empty.background)
                        } else {
                            setTextColor(titleCounterWidgetColor.counter.nonEmpty.label)
                            background.setTint(titleCounterWidgetColor.counter.nonEmpty.background)
                        }
                        text = itemCount.toString()
                    }
                }
            }
        }

        jobs.add(job)

        with(binding.titleCounterWidgetIcon) {
            setImageFrom(
                params.icon,
                origin.lifecycleScope,
                hideIfUnknown = true,
                hideIfLoading = true,
            )
            imageTintList = ColorStateList.valueOf(titleCounterWidgetColor.icon)
        }

        setOnSafeClickListener {
            App.track(
                WidgetEventAnalytics(
                    EventName.widgetCollectionWidgetTap(),
                    buildAnalytics(params, screenName)
                )
            )
            linkResolver.route(params.onTap.routeLink)?.let { route ->
                redirectTo(route, origin)
            }
        }
    }
}
