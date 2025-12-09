package com.greencopper.interfacekit.widgets.ui.fullwidthwidget

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.compose.LocalLocalizationAccess
import com.greencopper.interfacekit.ui.compose.rememberAsyncImagePainter
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageWidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator

internal class FullWidthImageWidgetGenerator(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: FullWidthImageWidgetParameters,
    origin: Layout,
) : WidgetGenerator {
    override val id: String? = null
    override val topPadding = 0
    override val bottomPadding = 0
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) =
        { modifier ->
        FullWidthImageWidget(
            params = params,
            onTap = params.onTap?.let {
                {
                    metrics.track(
                        WidgetEventAnalytics(
                            EventName.widgetCollectionWidgetTap(),
                            buildWidgetAnalytics(
                                FullWidthImageWidgetInitializer.widgetCategory,
                                params.analytics.itemName,
                                screenName
                            )
                        )
                    )

                    routeController.redirect(it, origin)
                }
            },
            modifier,
        )
    }
}

@Composable
internal fun FullWidthImageWidget(
    params: FullWidthImageWidgetParameters,
    onTap: (() -> Unit)?,
    modifier: Modifier,
) {
    val darkTheme = isSystemInDarkTheme()

    val imageName = if (darkTheme) {
        params.image.dark ?: params.image.light
    } else {
        params.image.light
    }
    val painter = rememberAsyncImagePainter(
        imageName = imageName
    )

    val accessibilityString: String? = LocalLocalizationAccess.current.getString(params.accessibilityLabel)
    val interactionSource = remember { MutableInteractionSource() }

    Image(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = accessibilityString.orEmpty()
            }
            .indication(
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .let { modifier ->
                if (onTap != null) {
                    modifier.clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onTap
                    )
                } else modifier.focusable(true, interactionSource)
            },
        painter = painter,
        contentDescription = accessibilityString,
        contentScale = ContentScale.FillWidth,
    )
}
