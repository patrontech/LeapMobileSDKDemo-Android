package com.greencopper.maps.textstyle

import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarTextStyle
import com.greencopper.interfacekit.lists.ui.EmptyViewTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.*

internal object MapsTextStyle : UITextStyle() {
    override val level: String = "maps"

    val geoMap = GeoMap(this)

    class GeoMap(parent: MapsTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "geoMap"

        val filters = FilteringBarTextStyle(this)
    }

    val locationDetail = LocationDetail(this)

    class LocationDetail(parent: MapsTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "locationDetail"

        val address: IKFont get() = toIKFont("address", IKFont.TextStyle.headlineM)

        val header = Header(this)

        class Header(parent: LocationDetail) : UITextStyle(parent) {
            override val level: String = "header"

            val name: IKFont get() = toIKFont("name", IKFont.TextStyle.titleL)
            val subtitle: IKFont get() = toIKFont("subtitle", IKFont.TextStyle.headlineS)
        }

        val description = Description(this)

        class Description(parent: LocationDetail) : UITextStyle(parent) {
            override val level: String = "description"

            val title: IKFont get() = toIKFont("title", IKFont.TextStyle.headlineL)
            val text: IKFont get() = toIKFont("text", IKFont.TextStyle.bodyL)
        }
    }

    val locationList = LocationList(this)

    class LocationList(parent: MapsTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "locationList"

        val filters = FilteringBarTextStyle(this)

        val cell = Cell(this)
        val empty = Empty(this)

        class Empty(parent: LocationList) : UITextStyle(parent), EmptyViewTextStyle {
            override val level: String = "empty"

            override val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            override val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyM)
        }

        class Cell(parent: LocationList) : UITextStyle(parent) {
            override val level: String = "cell"

            val name: IKFont get() = toIKFont("name", IKFont.TextStyle.headlineM)
            val subtitle: IKFont get() = toIKFont("subtitle", IKFont.TextStyle.headlineS)
        }
    }
}
