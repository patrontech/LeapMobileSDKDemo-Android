package com.greencopper.interfacekit.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.toWidgetItemsBySortedIndex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.util.*

public abstract class ListViewModel<T : KiboSerializable<T>>(
    protected val filteringHandler: FilteringHandler,
    protected val widgetResolver: WidgetResolver,
    protected val localizationService: LocalizationService,
    public val favoritesManager: FavoritesManager<*>,
    protected val locale: Locale,
) : ViewModel() {
    protected var resolvedWidgetItems: LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>>? =
        null

    /**
     * This will notify when a filter changes (either tag or mode)
     */
    public val filterChangingNotifier: Flow<Unit> =
        filteringHandler.predicate.shareIn(viewModelScope, SharingStarted.Eagerly).map {}

    public abstract fun getItems(widgetCollections: List<WidgetCollectionCellLayoutData>?): Flow<List<T>>

    public fun switchMode(newMode: FilteringHandler.Mode): Unit = filteringHandler.switchMode(newMode)

    public fun getMode(): FilteringHandler.Mode = filteringHandler.currentMode

    public fun getFilteringBarData(layout: Layout, screenName: String): FilteringBarData =
        filteringHandler.buildBarData(layout, screenName)

    public fun getCurrentFilterState(): SavedFiltering =
        SavedFiltering(filteringHandler.currentMode, filteringHandler.currentStateToInfo)

    protected fun getSortedWidgetItems(widgetCollections: List<WidgetCollectionCellLayoutData>): LinkedHashMap<Int, List<WidgetCollectionView.WidgetItem>> {
        return resolvedWidgetItems ?: widgetCollections.toWidgetItemsBySortedIndex(widgetResolver)
            .also {
                resolvedWidgetItems = it
            }
    }

    @Serializable
    public data class SavedFiltering(val mode: FilteringHandler.Mode, val filteringInfo: FilteringInfo? = null) :
        KiboSerializable<SavedFiltering> {
        override fun getSerializer(): KSerializer<SavedFiltering> = serializer()
    }
}
