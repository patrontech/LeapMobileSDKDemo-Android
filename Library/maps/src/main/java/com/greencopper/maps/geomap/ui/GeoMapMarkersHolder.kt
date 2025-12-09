package com.greencopper.maps.geomap.ui

import android.graphics.Bitmap
import android.graphics.Point
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.*
import com.google.maps.android.geometry.Bounds
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.maps.geomap.ui.MapItem.MapFeatureItem
import com.greencopper.maps.geomap.ui.MapItem.MapLabelItem
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

internal class GeoMapMarkersHolder {
    val featureMarkers: MutableMap<Marker, MapFeatureItem> = ConcurrentHashMap()
    val labelMarkers: MutableMap<Marker, MapLabelItem> = ConcurrentHashMap()

    val selectedFeature = MutableStateFlow<MapData.Feature?>(null)

    fun getAllMapItems() = featureMarkers.values.plus(labelMarkers.values)

    fun getFeatureFromMarker(marker: Marker): MapData.Feature? =
        (featureMarkers[marker] ?: labelMarkers[marker])?.feature

    fun collidesWithAnything(projection: Projection, labelItem: MapLabelItem): Boolean {
        val labelBounds = labelItem.itemBoundsWhenBeneath(projection)
        featureMarkers.values.forEach {
            if (it.itemBounds(projection).intersects(labelBounds)) return true
        }

        labelMarkers.values.minus(labelItem).forEach {
            if (it.itemBoundsWhenBeneath(projection).intersects(labelBounds)) return true
        }

        return false
    }

    fun remove(item: MapItem) {
        when (item) {
            is MapFeatureItem -> remove(item)
            is MapLabelItem -> remove(item)
        }
    }

    fun remove(item: MapFeatureItem) {
        featureMarkers.keys
            .filter { featureMarkers[it] == item }
            .forEach {
                it.remove()
                featureMarkers.remove(it)
            }
    }

    fun remove(item: MapLabelItem) {
        labelMarkers.keys
            .filter { labelMarkers[it] == item }
            .forEach {
                it.remove()
                labelMarkers.remove(it)
            }
    }
}

internal sealed class MapItem {
    abstract val feature: MapData.Feature

    internal class MapFeatureItem(
        override val feature: MapData.Feature,
        private val bitmap: Bitmap,
    ) : MapItem() {
        val bitmapDescriptor: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        val position: LatLng = feature.geometry.getPointCoordinates()

        fun itemBounds(projection: Projection): Bounds {
            val screenPoint = projection.toScreenLocation(position)

            // plus/minus 2 to provide some padding on the collision
            val topLeftPoint = Point(screenPoint.x - (bitmap.width / 2) + 2, screenPoint.y - (bitmap.height / 2) + 2)
            val bottomRightPoint =
                Point(screenPoint.x + (bitmap.width / 2) - 2, screenPoint.y + (bitmap.height / 2) - 2)

            return Bounds(
                topLeftPoint.x.toDouble(),
                bottomRightPoint.x.toDouble(),
                topLeftPoint.y.toDouble(),
                bottomRightPoint.y.toDouble()
            )
        }

        override fun equals(other: Any?): Boolean = (other as? MapFeatureItem)?.feature == feature

        override fun hashCode(): Int = feature.hashCode()
    }

    internal class MapLabelItem(
        override val feature: MapData.Feature,
        private val bitmap: Bitmap,
        private val drawnOffset: Int,
    ) : MapItem() {
        val bitmapDescriptor: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        val position: LatLng = feature.geometry.getPointCoordinates()

        fun itemBoundsWhenBeneath(projection: Projection): Bounds {
            val screenPoint = projection.toScreenLocation(position)

            // don't include the offset in the bounds, since nothing is actually drawn there
            val topLeftPoint = Point(screenPoint.x - (bitmap.width / 2), screenPoint.y + drawnOffset)
            val bottomRightPoint = Point(screenPoint.x + (bitmap.width / 2), screenPoint.y + bitmap.height)

            return Bounds(
                topLeftPoint.x.toDouble(),
                bottomRightPoint.x.toDouble(),
                topLeftPoint.y.toDouble(),
                bottomRightPoint.y.toDouble()
            )
        }

        override fun equals(other: Any?): Boolean = (other as? MapLabelItem)?.feature == feature

        override fun hashCode(): Int = feature.hashCode()
    }
}
