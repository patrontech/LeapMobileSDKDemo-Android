package com.greencopper.interfacekit.tabBar.viewmodel

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.tabBar.TabBarLayoutData
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class TabBarReducer(
    private val localizationService: LocalizationService,
) : Reducer<TabBarState, TabBarAction> {

    override fun reduce(
        state: TabBarState,
        action: TabBarAction,
    ): ReduceResult<TabBarState, TabBarAction> {
        return when (action) {
            is TabBarAction.LoadInitialTabData -> {
                val selectedIndex = if (state.selectedIndex < 0) action.data.defaultTabIndex else state.selectedIndex
                TabBarState(
                    itemStates = (action.data.items).mapIndexed { index, item ->
                        TabItemState(
                            text = localizationService.getString(item.name),
                            iconName = item.iconName,
                            isSelected = index == selectedIndex,
                            onClickAction = TabBarAction.TabSelected(index),
                            analytics = item.analytics,
                        )
                    },
                    selectedIndex = selectedIndex,
                ).withoutEffect()
            }
            is TabBarAction.TabSelectedByTag -> {
                val tagIndex = state.itemStates.indexOfFirst { it.text == action.tag }
                if (tagIndex >= 0) {
                    state.copyStateWithSelectedIndex(tagIndex).withoutEffect()
                } else {
                    state.withoutEffect()
                }
            }
            is TabBarAction.TabSelected ->
                state.copyStateWithSelectedIndex(action.index).withoutEffect()
            is TabBarAction.TabRedirected ->
                state.copyStateWithSelectedIndex(action.index).withoutEffect()
        }
    }

    private fun TabBarState.copyStateWithSelectedIndex(selectedIndex: Int) = this.copy(
        selectedIndex = selectedIndex,
        itemStates = this.itemStates.mapIndexed { index, itemState ->
            itemState.copy(
                isSelected = index == selectedIndex,
            )
        }
    )
}

@Serializable
internal sealed class TabBarAction {
    @Serializable data class LoadInitialTabData(val data: TabBarLayoutData) : TabBarAction()
    @Serializable data class TabSelected(val index: Int) : TabBarAction()
    @Serializable
    data class TabSelectedByTag(val tag: String) : TabBarAction()
    @Serializable data class TabRedirected(val index: Int) : TabBarAction()
}

@Serializable
internal data class TabBarState(
    val selectedIndex: Int,
    val itemStates: List<TabItemState>,
) : KiboSerializable<TabBarState> {
    override fun getSerializer(): KSerializer<TabBarState> = serializer()
}

@Serializable
internal data class TabItemState(
    val text: String,
    val iconName: String,
    val isSelected: Boolean,
    val onClickAction: TabBarAction.TabSelected,
    val analytics: ItemNameAnalytics,
) : KiboSerializable<TabItemState> {
    override fun getSerializer(): KSerializer<TabItemState> = serializer()
}
