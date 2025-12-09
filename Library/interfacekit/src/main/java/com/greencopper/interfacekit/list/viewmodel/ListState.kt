package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.empty.EmptyState
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.viewmodel.IndexedWidgets
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class ListState(
    @Transient val layout: Layout? = null,
    val filteringPredicate: FilteringPredicate.FilteringPredicateComputed? = null,
    val selectedList: Set<SelectedList> = emptySet(),
    @Transient val content: ViewState.ContentState? = null,
    @Transient val authorizedWidgets: List<IndexedWidgets> = emptyList(),
) : KiboSerializable<ListState> {
    override fun getSerializer(): KSerializer<ListState> = serializer()

    val isInMyFavorites: Boolean get() = selectedList.contains(SelectedList.MyFavorites)
    val isInMyInterests: Boolean get() = selectedList.contains(SelectedList.MyInterests)
}

internal object ViewState {
    interface ContentState {
        data class Content(
            val items: List<ListViewData>,
        ) : ContentState

        class Empty(
            title: String,
            subtitle: String,
            imageName: String,
            widgets: WidgetCollectionConfiguration.Instance?,
            screenName: String,
        ) : ContentState, EmptyState(title, subtitle, imageName, widgets, screenName)
    }
}

internal enum class SelectedList { MyFavorites, MyInterests }
