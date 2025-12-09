package com.greencopper.interfacekit.widgets.ui.cardcollectionwidget

import android.content.res.Resources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.imageservice.mockComposeImageService
import com.greencopper.interfacekit.metrics.widgetCollectionLinkTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.compose.*
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.pxToDp
import com.greencopper.interfacekit.ui.utils.createRect
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.CardCollectionItemStyle
import com.greencopper.interfacekit.widgets.initializer.CardCollectionWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.CardCollectionWidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator

internal data class CardCollectionState(
    val title: String?,
    val items: List<CardItemState>,
)

internal data class CardItemState(
    val style: CardCollectionWidgetParameters.Style,
    val size: CardSize,
    val label: String? = null,
    val accessibilityLabel: String,
    val onTap: String? = null,
    val analytics: CardCollectionWidgetParameters.Analytics,
)

internal data class CardSize(
    val widthPx: Float,
    val heightPx: Float,
)

internal fun calculateCardWidthHeight(resources: Resources): CardSize {
    val screenWidth = Resources.getSystem().displayMetrics.widthPixels.toFloat()
    val initialMargin = resources.getDimension(R.dimen.card_collection_content_padding)
    val spaceBetween = resources.getDimension(R.dimen.card_collection_item_spacing)
    val availableSpace = screenWidth - initialMargin - (3 * spaceBetween)
    val pixelsPerUnit = availableSpace / 345f // Want to show 3 whole cards, and then 45% of the next one
    val cardWidthPx = maxOf(100f * pixelsPerUnit, 78.dpToPx().toFloat())
    val cardHeightPx = (cardWidthPx * 15f) / 8f // Width to height ratio is 8w:15h

    return CardSize(cardWidthPx, cardHeightPx)
}

internal class CardCollectionWidgetGenerator(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: CardCollectionWidgetParameters,
    origin: Layout,
) : WidgetGenerator {
    override val id: String? = null
    override val topPadding = origin.resources.getInteger(R.integer.card_collection_vertical_padding)
    override val bottomPadding = origin.resources.getInteger(R.integer.card_collection_vertical_padding)
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) = { modifier ->
        CardCollectionWidget(
            routeController = routeController,
            metrics = metrics,
            screenName = screenName,
            params = params,
            origin = origin,
            modifier = modifier,
        )
    }
}

@Composable
internal fun CardCollectionWidget(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: CardCollectionWidgetParameters,
    origin: Layout,
    modifier: Modifier,
) {
    val cardSize = calculateCardWidthHeight(origin.resources)

    CardCollectionWidget(
        state = CardCollectionState(
            title = LocalLocalizationAccess.current.getString(params.title),
            items = params.items.map { item ->
                CardItemState(
                    style = item.style,
                    size = cardSize,
                    label = LocalLocalizationAccess.current.getString(item.label),
                    accessibilityLabel = LocalLocalizationAccess.current.getString(item.accessibilityLabel),
                    onTap = item.onTap,
                    analytics = item.analytics,
                )
            }
        ),
        onItemTap = { routeLink, analytics ->
            routeController.redirectRouteLink(routeLink, origin)
            metrics.track(
                WidgetEventAnalytics(
                    EventName.widgetCollectionLinkTap(),
                    buildWidgetAnalytics(
                        widgetCategory = CardCollectionWidgetInitializer.widgetCategory,
                        widgetName = analytics.itemName,
                        screenName = screenName,
                        itemId = analytics.itemId,
                    )
                )
            )
        },
        modifier = modifier,
    )
}

@Composable
internal fun CardCollectionWidget(
    state: CardCollectionState,
    onItemTap: (String, CardCollectionWidgetParameters.Analytics) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        state.title?.let { title ->
            Text(
                text = title,
                color = InterfaceKitColor.cardCollectionWidget.title,
                style = InterfaceKitTextStyle.cardCollectionWidget.title,
                modifier = Modifier.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 8.dp),
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.card_collection_item_spacing)),
            contentPadding = PaddingValues(horizontal = 24.dp),
        ) {
            items(state.items) { itemState ->
                CardWidget(
                    state = itemState,
                    onTap = onItemTap,
                )
            }
        }
    }
}

@Composable
internal fun CardWidget(
    state: CardItemState,
    onTap: (String, CardCollectionWidgetParameters.Analytics) -> Unit,
) {
    val colors = InterfaceKitColor.cardCollectionWidget.item

    val painter = when (state.style.type) {
        CardCollectionItemStyle.icon -> rememberAsyncImagePainter(state.style.icon ?: "")
        CardCollectionItemStyle.image -> rememberAsyncImagePainter(state.style.backgroundImage ?: "")
    }

    OutlinedCard(
        modifier = Modifier
            .width(state.size.widthPx.pxToDp().dp)
            .height(state.size.heightPx.pxToDp().dp)
            .let {
                if (state.onTap != null) {
                    it.clickable {
                        onTap(state.onTap, state.analytics)
                    }
                } else it
            },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = if (state.style.type == CardCollectionItemStyle.icon) 1.dp else 0.dp,
            color = colors.border,
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFFEFEFEF),
        ),

        ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.style.type) {
                CardCollectionItemStyle.image -> {
                    Image(
                        painter = painter,
                        contentDescription = state.accessibilityLabel,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }

                CardCollectionItemStyle.icon -> {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 18.dp, vertical = 24.dp)
                            .size(60.dp, 60.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        ) {
                            drawCircle(color = Color(0xFFB7B7B7), radius = size.maxDimension / 2)
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp, 2.dp)
                                .align(Alignment.Center),
                        ) {
                            drawCircle(color = Color.White, radius = size.maxDimension / 2)
                        }

                        Image(
                            painter = painter,
                            contentDescription = state.accessibilityLabel,
                            colorFilter = ColorFilter.tint(colors.icon.image),
                            modifier = Modifier
                                .size(40.dp, 40.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            state.label?.let { label ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.45f)
                        .align(Alignment.BottomCenter)
                        .drawWithCache {
                            val gradient = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color(0x88000000)),
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height)
                            )
                            onDrawBehind {
                                drawRect(gradient)
                            }
                        }
                )

                Text(
                    text = label,
                    color = colors.label,
                    style = InterfaceKitTextStyle.cardCollectionWidget.item.label,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 9.dp, vertical = 10.dp)
                        .semantics { invisibleToUser() },
                )
            }
        }
    }
}

@Preview
@Composable
private fun CardCollectionWidgetPreview() {
    val mockedColors = mapOf(
        InterfaceKitColor.cardCollectionWidget.getLevels("title") to Color.Black,
        InterfaceKitColor.cardCollectionWidget.item.getLevels("label") to Color.White,
        InterfaceKitColor.cardCollectionWidget.item.getLevels("background") to Color.Gray,
        InterfaceKitColor.cardCollectionWidget.item.getLevels("border") to Color.DarkGray,
        InterfaceKitColor.cardCollectionWidget.item.icon.getLevels("image") to Color.DarkGray,
        InterfaceKitColor.cardCollectionWidget.item.icon.getLevels("background") to Color.White,
        InterfaceKitColor.cardCollectionWidget.item.icon.getLevels("border") to Color.DarkGray,
    )
    val mockedImages = mapOf(
        "background" to createRect(width = 100.dp, height = 100.dp, color = Color.Cyan),
        "icon" to ContextCompat.getDrawable(LocalContext.current, R.drawable.ic_user_circle)!!,
    )
    MaterialTheme {
        MainCompositionLocalProvider(
            mockColors(mockedColors),
            LocalImageAccess provides mockComposeImageService(mockedImages),
        ) {
            CardCollectionWidget(
                CardCollectionState(
                    title = "Card Collection Widget",
                    items = listOf(
                        CardItemState(
                            style = CardCollectionWidgetParameters.Style(
                                type = CardCollectionItemStyle.image,
                                backgroundImage = "background",
                                icon = null,
                            ),
                            label = "With Image",
                            accessibilityLabel = "",
                            onTap = "",
                            size = CardSize(96f, 180f),
                            analytics = CardCollectionWidgetParameters.Analytics(""),
                        ),
                        CardItemState(
                            style = CardCollectionWidgetParameters.Style(
                                type = CardCollectionItemStyle.icon,
                                backgroundImage = null,
                                icon = "icon",
                            ),
                            label = "With Icon",
                            accessibilityLabel = "",
                            onTap = "",
                            size = CardSize(96f, 180f),
                            analytics = CardCollectionWidgetParameters.Analytics(""),
                        ),
                    )
                ),
                onItemTap = { _, _ -> },
                modifier = Modifier,
            )
        }
    }
}
