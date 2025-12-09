package com.greencopper.interfacekit.list.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.list.viewmodel.ListViewData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.compose.colorBackgroundOnClick
import com.greencopper.interfacekit.ui.compose.rememberAsyncImagePainter

@Composable
internal fun TableItem(
    itemData: ListViewData.ListItem,
    displayImage: Boolean,
    onCardTap: (Any) -> Unit = {},
    onFavoritesTap: (ListViewData.ListItem) -> Unit = {},
    modifier: Modifier,
) {
    val interactionsSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 92.dp)
            .colorBackgroundOnClick(interactionsSource, InterfaceKitColor.list.table.item.background)
            .clickable(interactionSource = interactionsSource) { onCardTap(itemData.id) }
            .padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (displayImage) {
            Image(
                painter = rememberAsyncImagePainter(itemData.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(1.dp, InterfaceKitColor.list.table.item.image.stroke)),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = itemData.title,
                color = InterfaceKitColor.list.table.item.title,
                style = InterfaceKitTextStyle.list.table.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            itemData.subtitle?.let {
                Text(
                    text = it,
                    color = InterfaceKitColor.list.table.item.subtitle,
                    style = InterfaceKitTextStyle.list.table.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        itemData.favIcon?.let {
            Icon(
                painter = rememberAsyncImagePainter(it),
                contentDescription = itemData.favIconDescription,
                tint = InterfaceKitColor.list.table.item.favoriteIcon,
                modifier = Modifier
                    .padding(8.dp)
                    .size(28.dp)
                    .clickable(interactionSource = null, indication = null) {
                        onFavoritesTap(itemData)
                    }
            )
        }
    }
}
