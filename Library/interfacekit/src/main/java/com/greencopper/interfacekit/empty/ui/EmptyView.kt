package com.greencopper.interfacekit.empty.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.databinding.EmptyViewBinding
import com.greencopper.interfacekit.empty.EmptyState
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toWidgetItems
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

public class EmptyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val binding = EmptyViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val localizationService: LocalizationService by App.lazy()
    private val widgetResolver: WidgetResolver by App.lazy()

    public fun setup(
        colors: EmptyViewColors,
        textStyles: EmptyViewTextStyles,
    ) {
        with(binding) {
            tvEmptyScheduleTitle.setTextColor(colors.title)
            tvEmptyScheduleTitle.setFont(textStyles.title)
            tvEmptyScheduleSubtitle.setTextColor(colors.subtitle)
            tvEmptyScheduleSubtitle.setFont(textStyles.subtitle)
        }
    }

    public fun fillIn(
        emptyState: EmptyState,
        origin: Layout,
        conditionChecker: ConditionChecker,
    ) {
        with(binding) {
            tvEmptyScheduleTitle.text = localizationService.getString(emptyState.title)
            tvEmptyScheduleSubtitle.text = localizationService.getString(emptyState.subtitle)
            ivEmptySchedule.setImageDrawable(null)
            ivEmptySchedule.setImageFrom(
                emptyState.imageName,
                origin.viewLifecycleOwner.lifecycleScope,
                hideIfUnknown = true,
                hideIfLoading = true,
            )

            origin.viewLifecycleOwner.lifecycleScope.launch {
                emptyState.topWidgetCollection?.widgets?.let {
                    widgetCollectionView.isVisible = true
                    widgetCollectionView.bind(
                        widgetItems = it.toWidgetItems(widgetResolver),
                        origin = origin,
                        screenName = emptyState.screenName,
                        conditionChecker = conditionChecker,
                    ).collect()
                } ?: run {
                    widgetCollectionView.isVisible = false
                }
            }
        }
    }
}
