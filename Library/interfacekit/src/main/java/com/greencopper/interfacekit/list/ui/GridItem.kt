package com.greencopper.interfacekit.list.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.list.viewmodel.ListViewData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.compose.rememberAsyncImagePainter

@Composable
internal fun GridItem(
    modifier: Modifier,
    itemData: ListViewData.ListItem,
    onCardTap: (Any) -> Unit = {},
    onFavoritesTap: (ListViewData.ListItem) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedCard(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.card_corner_radius)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = InterfaceKitColor.list.grid.item.background),
        border = BorderStroke(1.dp, InterfaceKitColor.list.grid.item.border),
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                4.dp,
                RoundedCornerShape(8.dp),
                ambientColor = InterfaceKitColor.list.grid.item.shadow,
                spotColor = InterfaceKitColor.list.grid.item.shadow,
            )
//            .scaleOnPress(interactionSource) //Disabled as it scrolls on press when BottomNavBar is present
            .semantics {
                contentDescription = itemData.title
            },
        interactionSource = interactionSource,
        onClick = {
            onCardTap(itemData.id)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            itemData.image?.let {
                val backgroundImage = rememberAsyncImagePainter(imageName = it)
                Image(
                    painter = backgroundImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(0.5f)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.64f to Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                itemData.favIcon?.let { favIcon ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = InterfaceKitColor.list.grid.item.favoriteButton.background
                            ),
                            modifier = Modifier
                                .size(50.dp)
                                .padding(8.dp)
                                .semantics {
                                    contentDescription = itemData.favIconDescription ?: ""
                                },
                            onClick = {
                                onFavoritesTap(itemData)
                            },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = rememberAsyncImagePainter(imageName = favIcon)
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = icon,
                                    tint = InterfaceKitColor.list.grid.item.favoriteButton.icon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = itemData.title,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = InterfaceKitColor.list.grid.item.label,
                    style = InterfaceKitTextStyle.list.grid.label,
                    modifier = Modifier
                        .padding(12.dp, 0.dp, 12.dp, 8.dp)
                        .semantics { invisibleToUser() }
                )
            }
        }
    }
}
