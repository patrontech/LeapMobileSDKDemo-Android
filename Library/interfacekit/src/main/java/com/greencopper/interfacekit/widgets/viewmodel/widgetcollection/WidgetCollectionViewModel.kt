package com.greencopper.interfacekit.widgets.viewmodel.widgetcollection

import androidx.lifecycle.ViewModel
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView.WidgetItem
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toWidgetItems

internal class WidgetCollectionViewModel(
    private val widgetResolver: WidgetResolver,
) : ViewModel() {

    private var resolvedWidgetItems: List<WidgetItem>? = null
    fun getWidgetItems(widgetInfos: List<WidgetCollectionConfiguration.Instance.WidgetInfo>): List<WidgetItem> {
        return resolvedWidgetItems ?: widgetInfos.toWidgetItems(widgetResolver).also {
            resolvedWidgetItems = it
        }
    }

}
