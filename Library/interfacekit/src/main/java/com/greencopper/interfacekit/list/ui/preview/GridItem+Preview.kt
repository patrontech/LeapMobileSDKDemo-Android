package com.greencopper.interfacekit.list.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.greencopper.interfacekit.list.ui.GridItem
import com.greencopper.interfacekit.list.viewmodel.ListAction
import com.greencopper.interfacekit.list.viewmodel.ListViewData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.ui.compose.*
import com.greencopper.interfacekit.ui.utils.createRect
import kotlin.random.Random

@Preview(
    showBackground = true,
    heightDp = 180,
    widthDp = 180,
)
@Composable
private fun Preview(@PreviewParameter(GridItemSampleProvider::class, 10) sample: GridItemSample) {
    val mockedColors = mapOf(
        InterfaceKitColor.list.grid.item.favoriteButton.getLevels("icon") to Color.Blue,
        InterfaceKitColor.list.grid.item.favoriteButton.getLevels("background") to Color.White,
        InterfaceKitColor.list.grid.item.getLevels("label") to Color.White,
    )
    val mockedTextStyles = mapOf(
        InterfaceKitTextStyle.list.empty.getLevels("title") to IKFont(IKFont.TextStyle.bodyXL),
        InterfaceKitTextStyle.list.empty.getLevels("subtitle") to IKFont(IKFont.TextStyle.titleL)
    )
    val createRect = createRect(width = 40.dp, height = 25.dp, color = Color.White)
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
            Box(propagateMinConstraints = false, contentAlignment = Alignment.Center) {
                Box(Modifier.size(sample.cardSize.dp, sample.cardSize.dp)) {
                    GridItem(
                        Modifier,
                        itemData = sample.item
                    )
                }
            }
        }
    }
}

private val itemValid: ListViewData.ListItem
    get() {
        val id = Random.Default.nextInt()
        val title = "Title"
        return ListViewData.ListItem(
            id = id,
            title = title,
            subtitle = "Subtitle",
            image = "image",
            favIcon = "favIcon",
            favIconDescription = "favorite icon accessibility",
            order = null,
            onFavoriteTapAction = ListAction.User.TappedAddToMyFavorites(id, title)
        )
    }

private val itemLongText = itemValid.copy(
    title = "Title that is pretty long to try to get to the 3 lines max ellipsize"
)

private data class GridItemSample(
    val item: ListViewData.ListItem,
    val cardSize: Int = 168,
)

public class GridItemSampleProvider : PreviewParameterProvider<Any> {
    override val values: Sequence<Any> = sequenceOf(
        GridItemSample(itemValid),
        GridItemSample(itemLongText),
        GridItemSample(itemLongText, cardSize = 100),
        GridItemSample(
            itemValid.copy(
                favIcon = null
            )
        ),
        GridItemSample(
            itemLongText.copy(
                favIcon = null
            )
        ),
    )
}
