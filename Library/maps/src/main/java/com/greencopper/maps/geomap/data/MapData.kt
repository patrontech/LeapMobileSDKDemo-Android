package com.greencopper.maps.geomap.data

import com.google.android.gms.maps.model.LatLng
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.navigation.route.Route
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
internal data class MapData(
    val features: List<Feature>,
) : KiboSerializable<MapData> {
    override fun getSerializer(): KSerializer<MapData> = serializer()

    @Serializable
    internal data class Feature(
        val geometry: Geometry,
        val properties: Properties
    )

    @Serializable
    internal data class Geometry(
        val type: FeatureType,
        private val coordinates: JsonArray,
    ) {
        fun getPointCoordinates(): LatLng {
            return LatLng(coordinates[1].jsonPrimitive.double, coordinates[0].jsonPrimitive.double)
        }

        fun getCoordinatesList(): List<LatLng> =
            coordinates
                .flatMap { it.jsonArray }
                .map { LatLng(it.jsonArray[1].jsonPrimitive.double, it.jsonArray[0].jsonPrimitive.double) }
    }

    @Serializable
    internal data class Properties(
        val locationId: String? = null,
        val onTap: Route? = null,
        val style: Style? = null,
        val zoomLevel: ZoomLevel? = null,
        val tags: List<String> = emptyList(),
        val imageName: String? = null,
        val analytics: Analytics? = null,
        val title: String? = null,
        val subtitle: String? = null,
    ) {
        @Serializable
        data class Analytics(
            val itemName: String,
            val itemId: Long,
        )
    }

    @Serializable
    internal data class Style(
        val type: String,
        val markerColor: Color? = null,
        val glyphColor: Color? = null,
        val glyphName: String? = null,
    )

    @Serializable
    internal data class ZoomLevel(
        val min: Int? = null,
        val max: Int? = null,
    )

    internal enum class FeatureType {
        Point,
        LineString,
        Polygon,
        MultiPoint,
        MultiLineString,
        MultiPolygon,
    }
}
