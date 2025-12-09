package com.greencopper.maps.geomap

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.maps.geomap.data.MapData
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

internal val geoMapTestData = GeoMapData(
    title = "kibomapscreen_name_356",
    analytics = ScreenNameAnalytics("Osheaga"),
    preSelectedLocationId = "{#/preSelectedLocationId?}",
    showUserLocation = true,
    camera = GeoMapData.Camera(
        isRotateEnabled = false,
        initial = GeoMapData.Camera.Initial(
            center = listOf(-73.53428942508322, 45.513002012853796),
            radius = 443f,
            bearing = 272f,
        ),
        restrictedArea = listOf(
            listOf(-73.53049626130881, 45.50892211485892),
            listOf(-73.538479485695, 45.509117483276064),
            listOf(-73.53808258885759, 45.517081910848674),
            listOf(-73.53009936447143, 45.51688657007763),
            listOf(-73.53049626130881, 45.50892211485892),
        ),
    ),
    geoJson = MapData(
        features = listOf(
            MapData.Feature(
                geometry = MapData.Geometry(
                    type = MapData.FeatureType.Point,
                    coordinates = JsonArray(listOf(JsonPrimitive( -73.53611045369293), JsonPrimitive(45.5099497804411))),
                ),
                properties = MapData.Properties(),
            ),
            MapData.Feature(
                geometry = MapData.Geometry(
                    type = MapData.FeatureType.Polygon,
                    coordinates = JsonArray(listOf(
                        JsonArray(listOf(
                        JsonArray(listOf(JsonPrimitive(-73.53049626130881), JsonPrimitive(45.50892211485892))),
                        JsonArray(listOf(JsonPrimitive(-73.538479485695), JsonPrimitive(45.509117483276064))),
                        JsonArray(listOf(JsonPrimitive(-73.53808258885759), JsonPrimitive(45.517081910848674))),
                        JsonArray(listOf(JsonPrimitive(-73.53009936447143), JsonPrimitive(45.51688657007763))),
                        JsonArray(listOf(JsonPrimitive(-73.53049626130881), JsonPrimitive(45.50892211485892))),
                    ))
                    )),
                ),
                properties = MapData.Properties(),
            )
        )
    )
)
