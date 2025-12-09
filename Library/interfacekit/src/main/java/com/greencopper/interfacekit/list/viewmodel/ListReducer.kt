package com.greencopper.interfacekit.list.viewmodel

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.favorites.FavoritesManager
import com.greencopper.interfacekit.favorites.toFavoriteable
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.provider.ListProvider
import com.greencopper.interfacekit.list.viewmodel.ListAction.*
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.utils.withMultipleFlowsEffect
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.viewmodel.getFlowOfIndexedWidgets
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.Logging
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withEffect
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.CoroutineContext

internal class ListReducer<Id : Any> private constructor(
    private val listProvider: ListProvider,
    private val favoritesManager: FavoritesManager<Id>,
    private val localizationService: LocalizationService,
    private val listData: ListLayoutData,
    private val widgetResolver: WidgetResolver,
    private val conditionChecker: ConditionChecker,
    private val logger: Logging,
    private val routeController: RouteController,
    private val interestsConfigHolder: InterestsConfigurationHolder,
    private val localStorage: LocalStorage,
    private val coroutineContext: CoroutineContext,
    private val json: Json,
    private val asId: (Any) -> Id?,
) : Reducer<ListState, ListAction> {

    companion object {
        inline fun <reified Id : Any> create(
            listProvider: ListProvider,
            favoritesManager: FavoritesManager<Id>,
            localizationService: LocalizationService,
            listData: ListLayoutData,
            widgetResolver: WidgetResolver,
            conditionChecker: ConditionChecker,
            logger: Logging,
            routeController: RouteController,
            interestsConfigHolder: InterestsConfigurationHolder,
            localStorage: LocalStorage,
            coroutineContext: CoroutineContext,
            json: Json,
        ) = ListReducer(
            listProvider,
            favoritesManager,
            localizationService,
            listData,
            widgetResolver,
            conditionChecker,
            logger,
            routeController,
            interestsConfigHolder,
            localStorage,
            coroutineContext,
            json,
        ) {
            it as? Id
        }
    }

    private val items: List<ListProvider.Element> by lazy {
        runBlocking(coroutineContext) {
            listProvider.getElements()
        }
    }

    private var uiClient: UiClient? = null

    override fun reduce(state: ListState, action: ListAction): ReduceResult<ListState, ListAction> {
        return when (action) {
            is ScreenLoaded -> {
                uiClient = action.uiClient
                if (state.layout != null) {
                    state.copy(
                        layout = action.layout,
                    ).withoutEffect()
                } else {
                    reloadItems(
                        state.copy(layout = action.layout)
                    ).withMultipleFlowsEffect(
                        listData.widgetCollections.getFlowOfIndexedWidgets(
                            widgetResolver,
                            conditionChecker,
                            action.layout,
                            listData.analytics.screenName,
                            logger,
                            { WidgetsChanged(it) }
                        ),
                        localStorage.project.interfaceKit.interestIds.state.map { User.InterestsUpdated(it) },
                        favoritesManager.favoriteIdsFlow.map { User.FavoritesIdsUpdated(it) },
                    )
                }.also { newState ->
                    updateConditionCheckerMetadata(newState.state.isInMyInterests)
                }
            }

            is User.TappedMyFavorites -> {
                state.selectedList.run {
                    if (action.selected) {
                        plus(SelectedList.MyFavorites)
                    } else {
                        minus(SelectedList.MyFavorites)
                    }
                }.let {
                    state.copy(selectedList = it).withoutEffect()
                }
            }

            is User.TappedMyInterests -> {
                state.selectedList.run {
                    if (action.selected) {
                        plus(SelectedList.MyInterests)
                    } else {
                        minus(SelectedList.MyInterests)
                    }.also {
                        updateConditionCheckerMetadata(action.selected)
                    }
                }.let {
                    state.copy(selectedList = it).withoutEffect()
                }
            }

            is User.TappedAddToMyFavorites -> {
                state.also {
                    (asId(action.listItemId))?.let { id ->
                        val item: Favoriteable<Id> = id.toFavoriteable()
                        favoritesManager.addToFavorites(item)
                    }
                }.withoutEffect()
            }

            is User.TappedRemoveFromMyFavorites -> {
                state.also {
                    (asId(action.listItemId))?.let { id ->
                        favoritesManager.removeFromFavorites(id.toFavoriteable())
                    }
                }.withoutEffect()
            }

            is User.TappedListItem -> {
                state.also {
                    val layout = state.layout ?: return@also
                    val id = with(action.listItemId) {
                        if (this is String) {
                            "\"$this\""
                        } else {
                            this.toString()
                        }
                    }
                    routeController.resolveRouteLink(
                        listData.onItemTapRouteLink,
                        layout,
                        mapOf(listData.routeLinkKeyId to id),
                    )
                }.withoutEffect()
            }

            is User.FavoritesIdsUpdated -> {
                listData.favoritesEditing?.let {
                    reloadItems(state).withEffect(ItemsReloaded)
                } ?: state.withoutEffect()
            }

            is User.InterestsUpdated -> {
                listData.myInterests?.takeIf { state.isInMyInterests }?.let {
                    reloadItems(state).withEffect(ItemsReloaded)
                } ?: state.withoutEffect()
            }

            is User.FilteringUpdated -> {
                reloadItems(state.copy(filteringPredicate = action.filteringPredicate))
                    .also { uiClient?.resetScroll?.invoke() }
                    .withEffect(ItemsReloaded)
            }

            is WidgetsChanged -> {
                reloadItems(
                    state.copy(
                        authorizedWidgets = action.widgets
                    )
                ).withEffect(ItemsReloaded)
            }

            else -> state.withoutEffect()
        }
    }

    private fun updateConditionCheckerMetadata(
        isMyInterestsSelected: Boolean,
    ) {
        conditionChecker.metadata.value = json.encodeToJsonElement(mapOf("myInterests" to isMyInterestsSelected))
    }

    private fun reloadItems(
        state: ListState,
    ): ListState {
        return reloadItems(
            state,
            listData,
            items,
            favoritesManager.favoriteIds,
            interestsConfigHolder.currentConfiguration.value?.interests ?: emptyList(),
            localStorage.project.interfaceKit.interestIds.value,
            localizationService,
            App.locale,
        )
    }

    internal data class UiClient(
        val resetScroll: () -> Unit,
    ) {
        companion object
    }
}
