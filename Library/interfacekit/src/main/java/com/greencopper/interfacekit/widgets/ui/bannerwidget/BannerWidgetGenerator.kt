package com.greencopper.interfacekit.widgets.ui.bannerwidget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
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
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.compose.*
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.BannerWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.BannerWidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator

internal class BannerWidgetGenerator(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: BannerWidgetParameters,
    origin: Layout,
) : WidgetGenerator {
    override val id: String? = null
    override val topPadding = 0
    override val bottomPadding = 0
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) = { modifier ->
        BannerWidget(
            state = BannerState(
                title = params.title,
                subtitle = params.subtitle,
                button = params.button?.let {
                    BannerState.Button(
                        text = it.text,
                        icon = it.icon,
                        onTap = {
                            routeController.redirectRouteLink(it.onTap.routeLink, origin)
                            metrics.track(
                                WidgetEventAnalytics(
                                    EventName.widgetCollectionWidgetTap(),
                                    buildWidgetAnalytics(
                                        widgetCategory = BannerWidgetInitializer.widgetCategory,
                                        widgetName = it.onTap.analytics.itemName,
                                        screenName = screenName,
                                    )
                                )
                            )
                        }
                    )
                }
            ),
            modifier = modifier,
        )
    }
}

internal data class BannerState(
    val title: String,
    val subtitle: String?,
    val button: Button?,
) {
    data class Button(
        val text: String,
        val icon: String,
        val onTap: () -> Unit,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun BannerWidget(
    state: BannerState,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .background(InterfaceKitColor.bannerWidget.border)
            .padding(bottom = 1.dp)
            .background(InterfaceKitColor.bannerWidget.background)
            .padding(end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 8.dp),
        ) {
            Text(
                text = LocalLocalizationAccess.current.getString(state.title),
                color = InterfaceKitColor.bannerWidget.title,
                style = InterfaceKitTextStyle.bannerWidget.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            state.subtitle?.let {
                Text(
                    text = LocalLocalizationAccess.current.getString(it),
                    color = InterfaceKitColor.bannerWidget.subtitle,
                    style = InterfaceKitTextStyle.bannerWidget.subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        state.button?.let {
            val text = LocalLocalizationAccess.current.getString(it.text)

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(interactionSource = null, indication = null) {
                        it.onTap()
                    }
                    .semantics { contentDescription = text },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = rememberAsyncImagePainter(it.icon),
                    contentDescription = text,
                    tint = InterfaceKitColor.bannerWidget.button.icon,
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { invisibleToUser() }
                )
                Text(
                    text = text,
                    color = InterfaceKitColor.bannerWidget.button.text,
                    style = InterfaceKitTextStyle.bannerWidget.button.text,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

    }
}

@Preview
@Composable
private fun BannerWidgetPreview() {
    val mockedColors = mapOf(
        InterfaceKitColor.bannerWidget.getLevels("title") to Color.Black,
        InterfaceKitColor.bannerWidget.getLevels("subtitle") to Color.Gray,
        InterfaceKitColor.bannerWidget.getLevels("background") to Color.White,
        InterfaceKitColor.bannerWidget.getLevels("border") to Color.DarkGray,
        InterfaceKitColor.bannerWidget.button.getLevels("text") to Color.White,
        InterfaceKitColor.bannerWidget.button.getLevels("icon") to Color.Gray,
    )
    val mockedImages = mapOf(
        "icon" to ContextCompat.getDrawable(LocalContext.current, R.drawable.ic_user_circle)!!,
    )
    MaterialTheme {
        MainCompositionLocalProvider(
            mockColors(mockedColors),
            LocalImageAccess provides mockComposeImageService(mockedImages),
        ) {
            BannerWidget(
                state = BannerState(
                    title = "Title",
                    subtitle = "Subtitle",
                    button = BannerState.Button(
                        text = "Button",
                        icon = "icon",
                        onTap = {},
                    ),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
