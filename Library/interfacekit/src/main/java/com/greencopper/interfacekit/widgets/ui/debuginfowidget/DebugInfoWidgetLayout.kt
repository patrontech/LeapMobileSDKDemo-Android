package com.greencopper.interfacekit.widgets.ui.debuginfowidget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageKey
import com.greencopper.core.localstorage.get
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.DebugInfoWidgetBinding
import com.greencopper.interfacekit.gestures.MorseGestureListener
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setTextOrGone
import com.greencopper.interfacekit.widgets.initializer.DebugInfoWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class DebugInfoWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RedirectingWidgetLayout<DebugInfoWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "debug_info_widget"
    override val binding = DebugInfoWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val localizationService: LocalizationService by App.lazy()
    private val localStorage: LocalStorage by App.lazy()
    private val draftContentManager: DraftContentManager by App.lazy()

    @Throws(WidgetException::class)
    override fun bind(params: DebugInfoWidgetParameters, screenName: String, origin: Layout, jobs: MutableList<Job>) {
        params.infos.forEach { info ->
            val textView = TextView(context)
            textView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            textView.gravity = Gravity.CENTER
            textView.setTextColor(InterfaceKitColor.debugInfoWidget.text)
            textView.setFont(InterfaceKitTextStyle.debugInfoWidget.text)
            computeInfo(info, textView)
            binding.debugInfoWidgetContainer.addView(textView)
        }

        params.morseRoute?.let { morseRoute ->
            val gestureListener = MorseGestureListener()
            gestureListener.init(morseRoute.sequence) {
                routeController.resolveRouteLink(morseRoute.routeLink, origin)
            }

            with (binding.debugInfoWidgetContainer) {
                isHapticFeedbackEnabled = false
                isSoundEffectsEnabled = false
                setOnClickListener { gestureListener.onClick() }
                setOnLongClickListener {
                    gestureListener.onLongClick()
                    true
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun computeInfo(
        info: DebugInfoWidgetParameters.Info,
        textView: TextView
    ) {
        when (info.type) {
            "localStorage" -> {
                val value = localizationService.getString(localStorage.localStorageContainer.get(LocalStorageKey(info.key), ""))
                textView.text = localizationService.getString(info.label)?.let { label ->
                    "$label $value"
                } ?: value
            }
            "computed" -> {
                textView.setTextOrGone(computedLabel(info.key))
            }
        }
    }

    private fun computedLabel(key: String): String? = when (key) {
        "isDraftContentEnabled" -> if (draftContentManager.passcode != null)
                localizationService.getString("interfaceKit.debugInfo.draftContentEnabled") else null
        else -> null
    }

    override fun getWidgetItemName(params: DebugInfoWidgetParameters): String =
        widgetCategory
}
