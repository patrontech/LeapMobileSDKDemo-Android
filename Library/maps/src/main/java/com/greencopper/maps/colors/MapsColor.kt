package com.greencopper.maps.colors

import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarColor
import com.greencopper.interfacekit.lists.ui.EmptyViewColors
import com.greencopper.interfacekit.tags.ui.TagColor
import com.greencopper.toolkit.App

internal object MapsColor : UIColor() {
    override val level: String = "maps"

    val geoMap = GeoMap(this)

    class GeoMap(parent: MapsColor) : ScreenColor(parent) {

        override val level: String = "geoMap"

        val filters = FilteringBarColor(this)

        val userLocationButton = UserLocationButton(this)

        class UserLocationButton(parent: GeoMap) : UIColor(parent) {
            override val level: String = "userLocationButton"

            val background get() = App.color(getLevels("background"), default.background.primary)
            val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
            val icon get() = App.color(getLevels("icon"), default.accent.primary)
        }

        val searchButton = SearchButton(this)

        class SearchButton(parent: GeoMap) : UIColor(parent) {
            override val level: String = "searchButton"

            val background get() = App.color(getLevels("background"), default.background.primary)
            val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
            val icon get() = App.color(getLevels("icon"), default.accent.primary)
        }

        val point = Point(this)

        class Point(parent: GeoMap) : UIColor(parent) {

            override val level: String = "point"

            val marker get() = App.color(getLevels("marker"), default.accent.secondary)
            val glyph get() = App.color(getLevels("glyph"), default.label.senary)
        }
    }

    val locationDetail = LocationDetail(this)

    class LocationDetail(parent: MapsColor) : ScreenColor(parent) {

        override val level: String = "locationDetail"

        val addressIcon get() = App.color(getLevels("addressIcon"), default.label.quaternary)
        val address get() = App.color(getLevels("address"), default.label.quaternary)
        val descriptionTitle get() = App.color(getLevels("descriptionTitle"), default.label.secondary)
        val description get() = App.color(getLevels("description"), default.label.quinary)

        val header = Header(this)

        class Header(parent: LocationDetail) : UIColor(parent) {
            override val level: String = "header"

            val swipeIndicator get() = App.color(getLevels("swipeIndicator"), default.fill.primary)
            val name get() = App.color(getLevels("name"), default.label.primary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
            val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
            val myLocationIcon
                get() = App.color(
                    getLevels("myLocationIcon"),
                    default.accent.primary
                )
        }

        val tags = TagColor(this)

        val image = Image(this)

        class Image(parent: LocationDetail) : UIColor(parent) {

            override val level: String = "image"

            val border get() = App.color(getLevels("border"), default.fill.secondary)
        }
    }

    val locationsList = LocationsList(this)

    class LocationsList(parent: MapsColor) : ScreenColor(parent) {
        override val level: String = "locationsList"
        val separator get() = App.color(getLevels("separator"), default.fill.primary)

        val cell = Cell(this)
        val filters = FilteringBarColor(this)
        val empty = Empty(this)

        class Empty(parent: LocationsList) : UIColor(parent), EmptyViewColors {
            override val level: String = "empty"
            override val title get() = App.color(getLevels("title"), default.label.primary)
            override val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
        }

        class Cell(parent: LocationsList) : UIColor(parent) {
            override val level = "cell"
            val name get() = App.color(getLevels("name"), default.label.secondary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
            val myLocationIcon get() = App.color(getLevels("myLocationIcon"), default.accent.primary)

            class Background(parent: Cell) : PressableColor(parent) {
                override val level = "background"
                override val normalDefault = default.background.primary
                override val pressedDefault = default.fill.tertiary
            }

            val background = Background(this)
        }
    }
}
