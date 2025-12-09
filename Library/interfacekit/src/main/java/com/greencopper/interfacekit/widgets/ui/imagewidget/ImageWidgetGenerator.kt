import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.compose.LocalLocalizationAccess
import com.greencopper.interfacekit.ui.compose.rememberAsyncImagePainter
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.ImageWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.ImageWidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator

internal class ImageWidgetGenerator(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: ImageWidgetParameters,
    origin: Layout,
) : WidgetGenerator {
    override val id: String? = null
    override val topPadding = origin.resources.getInteger(R.integer.widget_min_margin)
    override val bottomPadding = origin.resources.getInteger(R.integer.widget_min_margin)
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) = { modifier ->
        ImageWidget(
            imageLight = params.image.light,
            imageDark = params.image.dark,
            accessibilityLabel = params.accessibilityLabel,
            hideIfUnknownImage = false,
            hideIfLoadingImage = false,
            borderColor = InterfaceKitColor.imageWidget.card.border,
            shadowColor = InterfaceKitColor.imageWidget.card.shadow,
            onTap = params.onTap?.let {
                {
                    metrics.track(
                        WidgetEventAnalytics(
                            EventName.widgetCollectionWidgetTap(),
                            buildWidgetAnalytics(
                                ImageWidgetInitializer.widgetCategory,
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
internal fun ImageWidget(
    imageLight: String,
    imageDark: String?,
    accessibilityLabel: String?,
    hideIfUnknownImage: Boolean,
    hideIfLoadingImage: Boolean,
    borderColor: Color,
    shadowColor: Color,
    onTap: (() -> Unit)?,
    modifier: Modifier,
) {
    val darkTheme = isSystemInDarkTheme()

    val imageName = if (darkTheme) {
        imageDark ?: imageLight
    } else {
        imageLight
    }
    val painter = rememberAsyncImagePainter(
        imageName = imageName,
        hideIfUnknown = hideIfUnknownImage,
        hideIfLoading = hideIfLoadingImage,
    )
    val accessibilityString: String? = LocalLocalizationAccess.current.getString(accessibilityLabel)
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
            .shadow(
                dimensionResource(id = R.dimen.widget_image_cardview_elevation),
                RoundedCornerShape(dimensionResource(id = R.dimen.card_corner_radius)),
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .let {
                if (onTap != null) {
                    it
                        .semantics { contentDescription = accessibilityString ?: "" }
//                        .scaleOnPress(interactionSource) //Disabled as it scrolls on press when BottomNavBar is present
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            onTap.invoke()
                        }
                } else if (accessibilityString != null) {
                    it
                        .semantics(mergeDescendants = true) {}
                        .clearAndSetSemantics {
                            contentDescription = accessibilityString
                        }
                } else {
                    it.semantics {
                        this.invisibleToUser()
                    }
                }
            },
    ) {
        OutlinedCard(
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.card_corner_radius)),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = dimensionResource(id = R.dimen.widget_image_cardview_elevation)),
            border = BorderStroke(1.dp, borderColor),
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}
