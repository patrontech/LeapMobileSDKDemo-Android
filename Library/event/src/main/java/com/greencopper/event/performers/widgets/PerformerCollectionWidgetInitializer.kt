package com.greencopper.event.performers.widgets

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.event.performers.Performer
import com.greencopper.event.performers.data.repository.PerformerRepository
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.initializer.ImageCollectionWidgetParameters
import com.greencopper.interfacekit.widgets.initializer.WidgetInitializer
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.imagecollectionwidget.ImageCollectionWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class PerformerCollectionWidgetInitializer(
    private val performerRepository: PerformerRepository,
) : WidgetInitializer {
    companion object {
        val key = WidgetKey(name = "Event.Widget.PerformerCollection", version = 1)
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = ImageCollectionWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            val decodedParams = decodeFromJsonElement<PerformerCollectionWidgetParameters>(jsonWidgetParams)
            require(decodedParams.performers.isNotEmpty())
            val imageItemsList: List<ImageCollectionWidgetParameters.Item>
            runBlocking {
                val performers = performerRepository.getPerformers().first()
                imageItemsList = decodedParams.performers.mapNotNull { performerId ->
                    performers.firstOrNull { it.itemId == performerId }
                }.map {
                    it.toImageCollectionWidgetParams(decodedParams.onPerformerTap)
                }
            }
            ImageCollectionWidgetParameters(
                decodedParams.title,
                imageItemsList
            )
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        unimplemented()
    }
}

@Serializable
internal data class PerformerCollectionWidgetParameters(
    val title: String? = null,
    val performers: List<String>,
    val onPerformerTap: String,
) : KiboSerializable<PerformerCollectionWidgetParameters> {
    override fun getSerializer(): KSerializer<PerformerCollectionWidgetParameters> = serializer()
}

private fun Performer.toImageCollectionWidgetParams(onTapRouteLink: String): ImageCollectionWidgetParameters.Item {
    return ImageCollectionWidgetParameters.Item(
        imageName = photos.firstOrNull() ?: "",
        label = name,
        accessibilityName = name,
        onTap = ImageCollectionWidgetParameters.Item.OnTap(
            routeLink = "$onTapRouteLink?performerId=\"${itemId}\"",
            analytics = ItemNameAnalytics(name),
        )
    )
}
