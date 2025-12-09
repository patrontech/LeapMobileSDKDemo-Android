package com.greencopper.maps.geomap

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.*
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.maps.common.*
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.maps.geomap.ui.GeoMapFragment
import kotlinx.serialization.*

internal class GeoMapInitializer : ParameterizedFeatureInitializer<GeoMapData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Maps.GeoMap", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): GeoMapData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: GeoMapData): Layout {
        val data = GeoMapLayoutData(
            geoJson = params.geoJson,
            preSelectedLocationId = params.preSelectedLocationId,
            hideMapBackgroundColor = params.hideMapBackgroundColor,
            showUserLocation = params.showUserLocation,
            filtering = params.filtering,
            featureInfo = FeatureInfo(key, params.encodeToJsonElement()),
            search = GeoMapLayoutData.Search.build(params.search),
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params),
            title = params.title,
            camera = GeoMapLayoutData.Camera(
                isRotateEnabled = params.camera.isRotateEnabled,
                restrictedArea = params.camera.restrictedArea.map { it.toLatLng() }.toBounds(),
                initial = GeoMapLayoutData.Camera.Initial(
                    center = params.camera.initial.center.toLatLng(),
                    radius = params.camera.initial.radius,
                    bearing = params.camera.initial.bearing,
                ),
            )
        )

        return GeoMapFragment(data)
    }

    private fun List<Double>.toLatLng() = LatLng(get(1), get(0))

    override fun redirectionHashForParams(params: GeoMapData): RedirectionHash =
        RedirectionHash(key, params.analytics.screenName + (params.preSelectedLocationId ?: ""))
}

@Serializable
internal data class GeoMapData(
    val geoJson: MapData,
    val filtering: FilteringInfo? = null,
    val preSelectedLocationId: String? = null,
    val hideMapBackgroundColor: Color? = null,
    val showUserLocation: Boolean,
    val search: Search? = null,
    val analytics: ScreenNameAnalytics,
    val title: String,
    val camera: Camera,
) : KiboSerializable<GeoMapData> {
    override fun getSerializer(): KSerializer<GeoMapData> = serializer()

    @Serializable
    data class Camera(
        val isRotateEnabled: Boolean,
        val initial: Initial,
        val restrictedArea: List<List<Double>>,
    ) {
        @Serializable
        data class Initial(val center: List<Double>, val radius: Float, val bearing: Float)
    }

    @Serializable
    data class Search(@SerialName("onTap") val onTapRouteLink: String)
}

@Serializable
internal data class GeoMapLayoutData(
    val geoJson: MapData,
    val search: Search?,
    val preSelectedLocationId: String?,
    val hideMapBackgroundColor: Color?,
    val showUserLocation: Boolean,
    val filtering: FilteringInfo?,
    val featureInfo: FeatureInfo,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
    val title: String,
    val camera: Camera,
) : KiboSerializable<GeoMapLayoutData> {

    override fun getSerializer(): KSerializer<GeoMapLayoutData> = serializer()

    @Serializable
    data class Camera(
        val isRotateEnabled: Boolean,
        val initial: Initial,
        @Serializable(with = LatLngBoundsSerializer::class) val restrictedArea: LatLngBounds,
    ) {
        @Serializable
        data class Initial(
            @Serializable(with = LatLngSerializer::class) val center: LatLng,
            val radius: Float,
            val bearing: Float,
        )
    }

    @Serializable
    internal data class Search(val onTapRouteLink: String) {
        companion object {
            fun build(search: GeoMapData.Search?): Search? =
                search?.let {
                    Search(it.onTapRouteLink)
                }
        }
    }
}
