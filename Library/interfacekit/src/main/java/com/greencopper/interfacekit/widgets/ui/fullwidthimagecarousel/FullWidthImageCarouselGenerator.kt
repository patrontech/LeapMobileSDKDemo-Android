package com.greencopper.interfacekit.widgets.ui.fullwidthimagecarousel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.metrics.widgetCollectionLinkTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.compose.LocalLocalizationAccess
import com.greencopper.interfacekit.ui.compose.rememberAsyncImagePainter
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageCarouselWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageCarouselWidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.extensions.formatTemplate
import kotlinx.coroutines.launch


internal class FullWidthImageCarouselState(
    val title: String,
    val items: List<FullWidthImageCarouselItemState>,
    val aspectRatio: Float,
    val pagerState: PagerState,
)

internal data class FullWidthImageCarouselItemState(
    val imageName: String,
    val accessibilityLabel: String,
    val onTap: FullWidthImageCarouselWidgetParameters.OnTap?,
)

internal class FullWidthImageCarouselWidgetGenerator(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: FullWidthImageCarouselWidgetParameters,
    origin: Layout,
) : WidgetGenerator {
    override val id: String? = null
    override val topPadding = 0
    override val bottomPadding = 0
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) = { modifier ->
        FullWidthImageCarouselWidget(
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
internal fun FullWidthImageCarouselWidget(
    metrics: AggregateMetricsService,
    routeController: RouteController,
    screenName: String,
    params: FullWidthImageCarouselWidgetParameters,
    origin: Layout,
    modifier: Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { params.images.size })

    FullWidthImageCarouselWidget(
        state = FullWidthImageCarouselState(
            items = params.images.map { image ->
                FullWidthImageCarouselItemState(
                    imageName = image.imageName,
                    accessibilityLabel = LocalLocalizationAccess.current.getString(image.accessibilityLabel),
                    onTap = image.onTap,
                )
            },
            aspectRatio = params.ratio,
            pagerState = pagerState,
            title = "${LocalLocalizationAccess.current.getString(params.accessibilityLabel)} ${
                LocalLocalizationAccess.current.getString(
                    "interfaceKit.Widget.Carousel"
                )
            }"
        ),
        onItemTap = { onTap ->
            routeController.redirectRouteLink(onTap.routeLink, origin)
            metrics.track(
                WidgetEventAnalytics(
                    EventName.widgetCollectionLinkTap(),
                    buildWidgetAnalytics(
                        widgetCategory = FullWidthImageCarouselWidgetInitializer.widgetCategory,
                        widgetName = onTap.analytics.itemName,
                        screenName = screenName,
                    )
                )
            )
        },
        modifier = modifier,
    )
}

@Composable
internal fun FullWidthImageCarouselWidget(
    state: FullWidthImageCarouselState,
    onItemTap: (FullWidthImageCarouselWidgetParameters.OnTap) -> Unit,
    modifier: Modifier,
) {
    val animationScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val nextPageLabel = LocalLocalizationAccess.current.getString("common.next_page")
    val previousPageLabel = LocalLocalizationAccess.current.getString("common.previous_page")
    val description = LocalLocalizationAccess.current.getString("interfaceKit.widget.carousel.description")

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics(mergeDescendants = true) {
                contentDescription = description.formatTemplate(
                    state.title,
                    (state.pagerState.currentPage + 1).toString(),
                    state.items.size.toString(),
                    state.items[state.pagerState.currentPage].accessibilityLabel
                )

                customActions = listOf(
                    CustomAccessibilityAction(
                        label = nextPageLabel,
                        action = {
                            val canGoNext = state.pagerState.currentPage < state.items.size - 1
                            if (canGoNext) {
                                animationScope.launch {
                                    state.pagerState.animateScrollToPage(state.pagerState.currentPage + 1)
                                    focusRequester.requestFocus()
                                }
                                true
                            } else false
                        }
                    ),
                    CustomAccessibilityAction(
                        label = previousPageLabel,
                        action = {
                            val canGoPrev = state.pagerState.currentPage > 0
                            if (canGoPrev) {
                                animationScope.launch {
                                    state.pagerState.animateScrollToPage(state.pagerState.currentPage - 1)
                                    focusRequester.requestFocus()
                                }
                                true
                            } else false
                        }
                    )
                )
            },
    ) {
        Carousel(
            state = state,
            onItemTap = onItemTap,
        )
        PageIndicator(
            numPages = state.items.size,
            pagerState = state.pagerState,
        )
    }
}

@Composable
private fun Carousel(
    state: FullWidthImageCarouselState,
    onItemTap: (FullWidthImageCarouselWidgetParameters.OnTap) -> Unit,
) {
    HorizontalPager(
        state = state.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics {},
    ) { page ->
        val currentItem = state.items[page]
        var modifier = Modifier
            .aspectRatio(state.aspectRatio)
            .clearAndSetSemantics {}
        currentItem.onTap?.let { onTap ->
            modifier = modifier.clickable { onItemTap(onTap) }
        }
        Image(
            painter = rememberAsyncImagePainter(currentItem.imageName),
            contentDescription = currentItem.accessibilityLabel,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

private val dotSize = 8.dp
private val innerDotPadding = 4.dp
private val outerDotPadding = 6.dp
private val maxDotWidth = 44.dp

@Composable
private fun PageIndicator(
    numPages: Int,
    pagerState: PagerState,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        val dotsWidth = remember { (numPages - 1) * (dotSize.value + innerDotPadding.value) + maxDotWidth.value }
        val backgroundWidth = remember { dotsWidth + (outerDotPadding.value * 2) }

        Canvas(
            Modifier
                .height(16.dp)
                .width(backgroundWidth.dp)
        ) {
            drawRoundRect(
                color = Color(0x4D000000),
                cornerRadius = CornerRadius(x = 40f),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(innerDotPadding, Alignment.CenterHorizontally),
            modifier = Modifier.width(dotsWidth.dp),
        ) {
            for (numPage in 0 until numPages) {
                var offset = pagerState.getOffsetDistanceInPages(numPage)
                if (offset < 0) offset *= -1
                val percent = if (offset < 1) 1f - offset else 0f

                IndicatorDot(
                    minWidth = dotSize,
                    maxWidth = maxDotWidth,
                    percentFill = percent,
                )
            }
        }
    }
}

@Composable
private fun IndicatorDot(
    minWidth: Dp,
    maxWidth: Dp,
    percentFill: Float,
) {
    val added = (maxWidth.value - minWidth.value) * percentFill
    val width = minWidth.value + added
    Canvas(modifier = Modifier.size(width = width.dp, height = minWidth)) {
        drawRoundRect(
            color = Color.White,
            cornerRadius = CornerRadius(x = minWidth.value * 2),
        )
    }
}
