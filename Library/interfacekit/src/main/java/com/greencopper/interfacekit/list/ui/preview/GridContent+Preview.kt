package com.greencopper.interfacekit.list.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.imageservice.mockComposeImageService
import com.greencopper.interfacekit.list.initializer.ListMode
import com.greencopper.interfacekit.list.ui.ListContent
import com.greencopper.interfacekit.list.ui.fillWidthOfParent
import com.greencopper.interfacekit.list.viewmodel.ListAction
import com.greencopper.interfacekit.list.viewmodel.ListViewData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.ui.compose.*
import com.greencopper.interfacekit.ui.utils.createRect
import com.greencopper.interfacekit.widgets.viewmodel.MockWidgetGenerator
import kotlin.random.Random

@Composable
internal fun PreviewWidgetCollectionCell(
    topPadding: Int?,
    bottomPadding: Int?,
) {
    val rect = createRect(width = 1.dp, height = 1.dp, color = Color.LightGray)
    val painter = rememberDrawablePainter(drawable = rect)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .fillWidthOfParent(16.dp)
            .padding(
                top = (topPadding ?: 0).dp,
                bottom = (bottomPadding ?: 0).dp
            )
            .height(50.dp),
        contentScale = ContentScale.Crop
    )
}

@Preview(
    showBackground = true,
    heightDp = 576,
    widthDp = 375,
)
@Composable
private fun Preview(@PreviewParameter(GridContentSampleProvider::class, 3) gridSample: GridContentSample) {
    val mockedColors = mapOf(
        InterfaceKitColor.list.grid.item.favoriteButton.getLevels("icon") to Color.Blue,
        InterfaceKitColor.list.grid.item.favoriteButton.getLevels("background") to Color.White,
        InterfaceKitColor.list.grid.item.getLevels("label") to Color.White,
    )
    val mockedTextStyles = mapOf(
        InterfaceKitTextStyle.list.empty.getLevels("title") to IKFont(IKFont.TextStyle.bodyXL),
        InterfaceKitTextStyle.list.empty.getLevels("subtitle") to IKFont(IKFont.TextStyle.titleL)
    )
    val createRect = createRect(width = 40.dp, height = 40.dp, color = Color.White)
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
            ListContent(
                mode = gridSample.gridMode, listItems = gridSample.items,
            )
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

private val itemLongText
    get() = itemValid.copy(
        title = "Title that is pretty long to try to get to the 3 lines max ellipsize"
    )

private data class GridContentSample(
    val items: List<ListViewData>,
    val gridMode: ListMode.Grid,
)

public class GridContentSampleProvider : PreviewParameterProvider<Any> {
    override val values: Sequence<Any> = sequenceOf(
        GridContentSample(
            listOf(
                itemValid,
                itemValid,
                ListViewData.WidgetItem(1, MockWidgetGenerator()),
                itemLongText,
                itemValid,
                itemValid,
                itemLongText,
                itemLongText,
                itemValid,
                itemLongText,
                itemLongText,
            ),
            ListMode.Grid(2)
        ),
        GridContentSample(
            listOf(
                itemValid,
                itemValid,
                itemLongText,
                ListViewData.WidgetItem(1, MockWidgetGenerator()),
                ListViewData.WidgetItem(2, MockWidgetGenerator()),
                itemValid,
                itemValid,
                itemValid,
                itemLongText,
                itemLongText,
                itemValid,
                itemLongText,
                itemLongText,
                ListViewData.WidgetItem(1, MockWidgetGenerator()),
            ),
            ListMode.Grid(3)
        ),
        GridContentSample(
            listOf(
                ListViewData.WidgetItem(1, MockWidgetGenerator()),
                itemValid,
                itemValid,
                itemLongText,
                ListViewData.WidgetItem(2, MockWidgetGenerator()),
                ListViewData.WidgetItem(3, MockWidgetGenerator()),
                ListViewData.WidgetItem(4, MockWidgetGenerator()),
            ),
            ListMode.Grid(3)
        ),
    )
}
