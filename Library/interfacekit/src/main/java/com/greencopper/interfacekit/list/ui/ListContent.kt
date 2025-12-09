package com.greencopper.interfacekit.list.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.list.ui.preview.PreviewWidgetCollectionCell
import com.greencopper.interfacekit.list.viewmodel.ListViewData
import com.greencopper.interfacekit.widgets.ui.getTopBottomPaddingWidgetAware

@SuppressLint("ComposableNaming")
@Composable
/**
 * Setup a LazyGridState to scroll to the top when the LazyGrid is at the top and an item is inserted at index 0.
 */
public fun LazyGridState.setupScrollToTopWhenInsertAtTop(scrollList: Collection<*>) {
    val old = rememberSaveable { mutableStateOf<Boolean?>(null) }
    var isTop: Boolean = !canScrollBackward

    LaunchedEffect(scrollList) {
        snapshotFlow { !canScrollBackward }
            .collect { isAtTop ->
                old.value = isTop
                isTop = isAtTop
            }
    }

    LaunchedEffect(scrollList) {
        if (!isTop && old.value == true) {
            scrollToItem(0)
        }
    }
}

@Composable
internal fun ListContent(
    mode: ListMode,
    listItems: List<ListViewData>,
    gridState: LazyGridState = rememberLazyGridState(),
    onCardTap: (Any) -> Unit = {},
    onFavoritesTap: (ListViewData.ListItem) -> Unit = {},
) {
    when (mode) {
        is ListMode.Grid -> ListContent(
            listItems = listItems,
            gridState = gridState,
            columns = mode.columns,
            containerHorizontalPadding = 16,
            containerTopPadding = 16,
            containerBottomPadding = 16,
            itemToItemPadding = 8,
            itemToWidgetPadding = 16,
            itemCreator = { itemData, modifier ->
                GridItem(
                    modifier = modifier,
                    itemData = itemData,
                    onCardTap,
                    onFavoritesTap,
                )
            },
            itemSeparator = {}
        )

        is ListMode.Table -> ListContent(
            listItems = listItems,
            gridState = gridState,
            columns = mode.columns,
            containerHorizontalPadding = 0,
            containerTopPadding = 0,
            containerBottomPadding = 0,
            itemToItemPadding = 0,
            itemToWidgetPadding = 0,
            itemCreator = { itemData, modifier ->
                TableItem(
                    itemData = itemData,
                    displayImage = mode.displayImages,
                    onCardTap,
                    onFavoritesTap,
                    modifier = modifier,
                )
            },
            itemSeparator = {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    thickness = 1.dp,
                    color = InterfaceKitColor.list.table.item.separator
                )
            }
        )
    }
}

@Composable
private fun ListContent(
    listItems: List<ListViewData>,
    gridState: LazyGridState = rememberLazyGridState(),
    columns: Int,
    containerHorizontalPadding: Int,
    containerTopPadding: Int,
    containerBottomPadding: Int,
    itemToItemPadding: Int,
    itemToWidgetPadding: Int,
    itemCreator: @Composable (ListViewData.ListItem, Modifier) -> Unit,
    itemSeparator: @Composable () -> Unit,
) {
    gridState.setupScrollToTopWhenInsertAtTop(scrollList = listItems)

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = remember { PaddingValues(start = containerHorizontalPadding.dp, end = containerHorizontalPadding.dp) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        listItems.forEachIndexed { index, listItem ->
            val topBottomPadding = getTopBottomPaddingWidgetAware(
                listItems = listItems,
                index = index,
                columnFactor = columns,
                containerTopPadding = containerTopPadding,
                containerBottomPadding = containerBottomPadding,
                itemToItemPadding = itemToItemPadding,
                itemToWidgetPadding = itemToWidgetPadding,
            )

            when (listItem) {
                is ListViewData.ListItem -> {
                    item(
                        key = listItem.id,
                    ) {
                        itemCreator(
                            listItem,
                            Modifier.padding(
                                top = (topBottomPadding.first).dp,
                                bottom = (topBottomPadding.second).dp,
                            )
                        )
                    }

                    listItems.getOrNull(index + 1)?.takeIf { columns == 1 && it is ListViewData.ListItem }?.let {
                        item(key = "${listItem.id}Separator") { itemSeparator() }
                    }
                }

                is ListViewData.WidgetItem -> {
                    item(
                        key = listItem.id,
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        if (LocalInspectionMode.current) {
                            PreviewWidgetCollectionCell(topBottomPadding.first, topBottomPadding.second)
                        } else {
                            listItem.widget.generateComposable(
                                Modifier
                                    .fillWidthOfParent(containerHorizontalPadding.dp)
                                    .padding(top = topBottomPadding.first.dp, bottom = topBottomPadding.second.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Used to extend the width of an item to the bounds of its parent, ignoring any padding placed by the parent.
 */
internal fun Modifier.fillWidthOfParent(parentHorizontalPadding: Dp) = this.layout { measurable, constraints ->
    val looseConstraints = constraints.offset(parentHorizontalPadding.roundToPx() * 2, 0)
    val placeable = measurable.measure(looseConstraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, 0)
    }
}
