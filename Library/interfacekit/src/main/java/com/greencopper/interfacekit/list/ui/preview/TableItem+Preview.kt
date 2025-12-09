package com.greencopper.interfacekit.list.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.imageservice.mockComposeImageService
import com.greencopper.interfacekit.list.ui.TableItem
import com.greencopper.interfacekit.list.viewmodel.ListAction
import com.greencopper.interfacekit.list.viewmodel.ListViewData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.ui.compose.*
import com.greencopper.interfacekit.ui.utils.createRect

@Preview(
    showBackground = true,
    backgroundColor = 0xFF919091,
    heightDp = 150,
    widthDp = 375,
)
@Composable
private fun PreviewTableItem(@PreviewParameter(TableItemSampleProvider::class, 10) sample: TableItemSample) {
    val mockedColors = mapOf(
        InterfaceKitColor.list.table.item.getLevels("favoriteIcon") to Color.Blue,
        InterfaceKitColor.list.table.item.getLevels("title") to Color.Red,
        InterfaceKitColor.list.table.item.getLevels("subtitle") to Color.Blue,
        InterfaceKitColor.list.table.item.background.getLevels("normal") to Color.White,
        InterfaceKitColor.list.table.item.background.getLevels("pressed") to Color.Gray,
    )
    val mockedTextStyles = mapOf(
        InterfaceKitTextStyle.list.empty.getLevels("title") to IKFont(IKFont.TextStyle.headlineM),
        InterfaceKitTextStyle.list.empty.getLevels("subtitle") to IKFont(IKFont.TextStyle.headlineS)
    )
    val createRect = createRect(width = 40.dp, height = 25.dp, color = Color.Green)
    val icon = ContextCompat.getDrawable(LocalContext.current, R.drawable.ic_search)!!
    val mockedImages = mapOf(
        "image" to createRect,
        "favIcon" to icon
    )

    MaterialTheme {
        MainCompositionLocalProvider(
            mockColors(mockedColors),
            mockTextStyle(mockedTextStyles),
            mockStrings(),
            LocalImageAccess provides mockComposeImageService(mockedImages),
        ) {
            TableItem(
                itemData = sample.itemData,
                displayImage = sample.displayImage,
                modifier = Modifier,
            )
        }
    }
}

private val itemValid = ListViewData.ListItem(
    title = "Title",
    subtitle = "Subtitle",
    image = "image",
    order = 0,
    id = 1,
    favIcon = "favIcon",
    favIconDescription = "favIconDescription",
    onFavoriteTapAction = ListAction.User.TappedAddToMyFavorites(1, "Title")
)

private val itemLongText = itemValid.copy(
    title = "Title with a very long text that should be truncated",
    subtitle = "Subtitle with a very long text that should be truncated",
)

private data class TableItemSample(
    val itemData: ListViewData.ListItem,
    val displayImage: Boolean = true,
)

public class TableItemSampleProvider : PreviewParameterProvider<Any> {
    override val values: Sequence<Any> = sequenceOf(
        TableItemSample(itemValid),
        TableItemSample(itemLongText),
        TableItemSample(itemValid, displayImage = false),
    )
}
