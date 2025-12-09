package com.greencopper.interfacekit.widgets.viewmodel

import com.greencopper.core.conditions.authorizedFlow
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionCellLayoutData
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.w
import kotlinx.coroutines.flow.*

public data class IndexedWidgets(
    val index: Int,
    val widgets: List<WidgetGenerator>,
)

public fun <Action : Any> List<WidgetCollectionCellLayoutData>?.getFlowOfIndexedWidgets(
    widgetResolver: WidgetResolver,
    conditionChecker: ConditionChecker,
    origin: Layout,
    screenName: String,
    logger: Logging,
    toAction: (List<IndexedWidgets>) -> Action,
): Flow<Action> {
    val listOfFlows = this
        ?.takeIf { it.isNotEmpty() }
        ?.distinctBy { it.index }
        ?.sortedBy { it.index }
        ?.map { widgetCollectionCellLayoutData ->
            widgetCollectionCellLayoutData.collection.widgets
                .authorizedFlow<WidgetCollectionConfiguration.Instance.WidgetInfo>(conditionChecker)
                .map { authorizedWidgetInfos ->
                    authorizedWidgetInfos
                        .mapNotNull { widgetInfo ->
                            try {
                                widgetResolver.resolveGenerator(widgetInfo, screenName, origin)
                            } catch (throwable: Throwable) {
                                logger.w("Problem parsing widget $widgetInfo", throwable = throwable)
                                null
                            }
                        }
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            widgetCollectionCellLayoutData.index to it
                        }
                }
        } ?: listOf(flowOf(null))

    return combine(listOfFlows) {
        it.toList().filterNotNull().map { (index, widgets) ->
            IndexedWidgets(index, widgets)
        }
    }.map {
        toAction(it)
    }
}
