package com.greencopper.interfacekit.widgets.ui.textwidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.localizationService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.TextWidgetBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.ClickableLinkMovementMethod
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.initializer.TextWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.decodeHtmlString
import kotlinx.coroutines.Job

internal class TextWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WidgetLayout<TextWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "text_widget"
    override val binding = TextWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val localizationService by lazy { App.localizationService() }

    init {
        setupWidth()
    }

    @Throws(WidgetException::class)
    override fun bind(
        params: TextWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        val localizedDescription = localizationService.getString(params.text)

        with(binding.text) {
            text = localizedDescription.decodeHtmlString()
            setTextColor(InterfaceKitColor.textWidget.text)
            setFont(InterfaceKitTextStyle.textWidget.text)
            movementMethod = ClickableLinkMovementMethod()
        }
    }

    override fun getWidgetItemName(params: TextWidgetParameters): String? = null

    companion object {
        val key = WidgetKey(name = "InterfaceKit.Widget.Text", version = 1)
    }
}
