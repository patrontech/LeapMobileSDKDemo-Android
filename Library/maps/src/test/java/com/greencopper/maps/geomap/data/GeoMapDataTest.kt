package com.greencopper.maps.geomap.data

import com.google.android.gms.maps.model.LatLng
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.maps.geomap.GeoMapData
import com.greencopper.maps.geomap.geoMapTestData
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GeoMapDataTest {

    @BeforeEach
    fun setUp() {
        Toolkit.setupTest()
    }

    @Test
    fun geoMapData_kiboSerializable() {
        testKiboSerializable(geoMapTestData)
    }

    @Test
    fun getPointCoordinates() {
        val feature = geoMapTestData.geoJson.features.first { it.geometry.type == MapData.FeatureType.Point }
        val latLng = feature.geometry.getPointCoordinates()
        assertThat(latLng.latitude).isEqualTo(45.5099497804411)
        assertThat(latLng.longitude).isEqualTo(-73.53611045369293)
    }

    @Test
    fun getCoordinatesList() {
        val feature = geoMapTestData.geoJson.features.first { it.geometry.type == MapData.FeatureType.Polygon }
        val list = feature.geometry.getCoordinatesList()

        val latLng1 = LatLng(45.50892211485892, -73.53049626130881)
        val latLng2 = LatLng(45.509117483276064, -73.538479485695)
        assertThat(list).contains(latLng1)
        assertThat(list).contains(latLng2)
    }
}
