package com.greencopper.interfacekit.list.viewmodel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarData
import com.greencopper.interfacekit.filtering.filteringbar.FilteringBarState
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarCell
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.viewmodel.ListAction.ScreenLoaded
import com.greencopper.interfacekit.list.viewmodel.ListAction.User
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.interfacekit.widgets.viewmodel.WidgetItemHolder
import com.toggl.komposable.architecture.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ListViewModel(
    private val filteringHandler: FilteringHandler,
    val viewBuilder: IKViewBuilder,
    private val listData: ListLayoutData,
    val conditionChecker: ConditionChecker,
    private val store: Store<ListState, ListAction>,
    private val scope: CoroutineScope,
) : ViewModel() {

    internal var filteringUpdater =
        filteringHandler.predicate.shareIn(scope, SharingStarted.Lazily)

    init {
        scope.launch {
            filteringUpdater.collectLatest {
                store.send(User.FilteringUpdated(it))
            }
        }

        scope.launch {
            store.state.map { state ->
                state.selectedList
            }.stateIn(scope, SharingStarted.Lazily, emptySet()).collectLatest {
                filteringHandler.switchMode(
                    if (it.contains(SelectedList.MyFavorites))
                        FilteringHandler.Mode.MY_FAVORITES
                    else
                        FilteringHandler.Mode.DEFAULT
                )
            }
        }
    }

    internal fun initialSetup(layout: Layout, uiClient: ListReducer.UiClient) {
        store.send(ScreenLoaded(layout, uiClient))
    }

    internal fun getFilteringBarButtons(): List<FilteringBarState.ButtonState> {
        val buttons = mutableListOf<FilteringBarState.ButtonState>()
        val state = runBlocking { store.state.first() }
        listData.myFavorites?.filteringButton?.let { filteringButton ->
            val defaultState = FilteringBarCell.ButtonState.State(
                filteringButton.unselected.title,
                filteringButton.unselected.icon,
                filteringButton.unselected.accessibilityLabel
            )
            val selectedState = FilteringBarCell.ButtonState.State(
                filteringButton.selected.title,
                filteringButton.selected.icon,
                filteringButton.selected.accessibilityLabel
            )

            buttons.add(
                FilteringBarState.ButtonState(
                    defaultState,
                    selectedState,
                    state.isInMyFavorites,
                ) { filterMyFavorites ->
                    store.send(
                        User.TappedMyFavorites(filterMyFavorites)
                    )
                }
            )
        }

        listData.myInterests?.filteringButton?.let { filteringButton ->
            val defaultState = FilteringBarCell.ButtonState.State(
                filteringButton.unselected.title,
                filteringButton.unselected.icon,
                filteringButton.unselected.accessibilityLabel
            )
            val selectedState = FilteringBarCell.ButtonState.State(
                filteringButton.selected.title,
                filteringButton.selected.icon,
                filteringButton.selected.accessibilityLabel
            )

            buttons.add(
                FilteringBarState.ButtonState(
                    defaultState,
                    selectedState,
                    state.isInMyInterests,
                ) { filterMyInterests ->
                    store.send(
                        User.TappedMyInterests(filterMyInterests)
                    )
                }
            )
        }


        return buttons
    }

    val contentState: Flow<ViewState.ContentState?> = store.state.map { state ->
        state.content
    }.stateIn(scope, SharingStarted.Lazily, null)

    internal fun onListItemTap(itemId: Any) {
        store.send(User.TappedListItem(itemId))
    }

    internal fun sendAction(action: ListAction) {
        store.send(action)
    }

    internal fun saveState(outState: Bundle, key: String) {
        val state = runBlocking {
            store.state.first()
        }
        outState.putKiboSerializable(key, state)
    }

    fun getFilteringBarData(layout: Layout): FilteringBarData =
        filteringHandler.buildBarData(layout, listData.analytics.screenName)

    fun getCurrentFilterState(): SavedFiltering =
        SavedFiltering(filteringHandler.currentStatesToInfoMap)

    @Serializable
    data class SavedFiltering(val filteringInfoMap: Map<FilteringHandler.Mode, FilteringInfo?> = emptyMap()) :
        KiboSerializable<SavedFiltering> {
        override fun getSerializer(): KSerializer<SavedFiltering> = serializer()
    }
}

internal sealed class ListViewData {
    data class ListItem(
        val id: Any,
        val title: String,
        val subtitle: String? = null,
        val image: String?,
        val favIcon: String?,
        val favIconDescription: String?,
        val order: Int? = null,
        val onFavoriteTapAction: ListAction,
    ) : ListViewData()

    data class WidgetItem(
        val id: Int,
        val widget: WidgetGenerator,
    ) : ListViewData(), WidgetItemHolder {
        override val topPadding: Int = widget.topPadding
        override val bottomPadding: Int = widget.topPadding

    }
}
