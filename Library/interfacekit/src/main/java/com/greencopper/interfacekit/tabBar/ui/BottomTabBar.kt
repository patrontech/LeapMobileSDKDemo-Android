package com.greencopper.interfacekit.tabBar.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.imageservice.mockComposeImageService
import com.greencopper.interfacekit.tabBar.viewmodel.TabBarAction
import com.greencopper.interfacekit.tabBar.viewmodel.TabBarState
import com.greencopper.interfacekit.tabBar.viewmodel.TabItemState
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.ui.compose.LocalImageAccess
import com.greencopper.interfacekit.ui.compose.MainCompositionLocalProvider
import com.greencopper.interfacekit.ui.compose.bounceOnClick
import com.greencopper.interfacekit.ui.compose.mockColors
import com.greencopper.interfacekit.ui.compose.mockTextStyle
import com.greencopper.interfacekit.ui.compose.rememberAsyncImagePainter


@Composable
internal fun BottomTabBar(state: TabBarState, onItemClick: (TabBarAction.TabSelected) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(54.dp)
            .fillMaxWidth()
            .background(color = InterfaceKitColor.bottomBar.backgroundComposable)
            .padding(6.dp)
    ) {
        state.itemStates.forEachIndexed { index, item ->
            TabBarIcon(item, onItemClick)
        }
    }
}

@Composable
private fun TabBarIcon(
    item: TabItemState,
    onItemClick: (TabBarAction.TabSelected) -> Unit,
) {
    val textStyles = InterfaceKitTextStyle.tabBar.item
    val colors = InterfaceKitColor.bottomBar.item
    val interactionSource = remember { MutableInteractionSource() }

    val painter = rememberAsyncImagePainter(
        item.iconName,
        hideIfUnknown = true,
        hideIfLoading = true,
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null) { onItemClick(item.onClickAction) }
            .bounceOnClick(interactionSource)
            .semantics(mergeDescendants = true) { }
    ) {
        Image(
            painter = painter,
            colorFilter = ColorFilter.tint(if (item.isSelected) colors.selected else colors.normal),
            contentDescription = null,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.size(2.dp))

        Text(
            text = item.text,
            style = if (item.isSelected) textStyles.selected else textStyles.normal,
            color = if (item.isSelected) colors.selected else colors.normal,
        )
    }
}

@Preview
@Composable
private fun BottomTabBarPreview() {
    val mockedColors = mapOf(
        InterfaceKitColor.bottomBar.getLevels("background") to Color(0xff6232a8),
        InterfaceKitColor.bottomBar.item.getLevels("selected") to Color.White,
        InterfaceKitColor.bottomBar.item.getLevels("normal") to Color.Gray,
    )
    val mockedTextStyles = mapOf(
        InterfaceKitTextStyle.tabBar.item.getLevels("selected") to IKFont(IKFont.TextStyle.footnoteM),
        InterfaceKitTextStyle.tabBar.item.getLevels("normal") to IKFont(IKFont.TextStyle.footnoteS),
    )

    val icon = ContextCompat.getDrawable(LocalContext.current, R.drawable.ic_user_circle)!!
    val mockedImages = mapOf(
        "icon" to icon
    )

    MaterialTheme {
        MainCompositionLocalProvider(
            mockColors(mockedColors),
            mockTextStyle(mockedTextStyles),
            LocalImageAccess provides mockComposeImageService(mockedImages),
        ) {
            BottomTabBar(
                TabBarState(
                    selectedIndex = 0,
                    itemStates = listOf(
                        TabItemState("Tab Item 1", "icon", true, onClickAction = TabBarAction.TabSelected(0), ItemNameAnalytics("")),
                        TabItemState("Tab Item 2", "icon", false, onClickAction = TabBarAction.TabSelected(1), ItemNameAnalytics("")),
                        TabItemState("Tab Item 3", "icon", false, onClickAction = TabBarAction.TabSelected(2), ItemNameAnalytics("")),
                    ),
                ),
                { },
            )
        }
    }
}
