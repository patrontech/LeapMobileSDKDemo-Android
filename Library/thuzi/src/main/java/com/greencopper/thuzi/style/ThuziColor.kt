package com.greencopper.thuzi.style

import androidx.compose.runtime.Composable
import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.color.UIColor
import com.greencopper.toolkit.App

internal object ThuziColor : UIColor() {
    override val level: String = "thuzi"

    val registration = Registration(this)
    class Registration(parent: ThuziColor) : ScreenColor(parent) {
        override val level: String = "registration"
    }

    val badges = Badges(this)
    class Badges(parent: ThuziColor) : ScreenColor(parent) {
        override val level: String = "badges"

        val header = Header(this)

        class Header(parent: Badges) : UIColor(parent) {
            override val level: String = "header"
            val background get() = App.color(getLevels("background"), default.topBar.background)
            val title get() = App.color(getLevels("title"), default.topBar.title)
            val description get() = App.color(getLevels("description"), default.topBar.title)
            val number get() = App.color(getLevels("number"), default.topBar.title)
            val button = Button(this)

            class Button(parent: Header) : UIColor(parent) {
                override val level: String = "button"
                val background get() = App.color(getLevels("background"), default.topBar.background)
                val icon get() = App.color(getLevels("icon"), default.topBar.item)
            }
        }

        val card = Card(this)

        class Card(parent: Badges) : UIColor(parent) {
            override val level: String = "card"

            val background get() = App.color(getLevels("background"), default.background.secondary)
            val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
            val border get() = App.color(getLevels("border"), default.background.secondary)
            val label = Label(this)

            class Label(parent: Card) : UIColor(parent) {
                override val level: String = "label"
                val locked get() = App.color(getLevels("background"), default.label.tertiary)
                val unlocked get() = App.color(getLevels("background"), default.label.secondary)
            }
        }

        val detail = Detail(this)

        class Detail(parent: Badges) : UIColor(parent) {
            override val level: String = "detail"

            val background get() = App.color(getLevels("background"), default.background.secondary)
            val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
            val close get() = App.color(getLevels("close"), default.accent.primary)
            val title get() = App.color(getLevels("title"), default.label.primary)
            val description get() = App.color(getLevels("description"), default.label.secondary)
        }
    }

    val fanscan = Fanscan(this)
    class Fanscan(parent: ThuziColor) : ScreenColor(parent) {
        override val level: String = "fanscan"

        val permissions = Permissions(this)

        class Permissions(parent: Fanscan): UIColor(parent) {
            override val level: String = "permissions"

            val title get() = App.color(getLevels("title"), default.label.secondary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
            val description get() = App.color(getLevels("description"), default.label.tertiary)
            val icon get() = App.color(getLevels("icon"), default.label.secondary)
            val background get() = App.color(getLevels("background"), default.background.primary)

            val button = Button(this)

            class Button(parent: Permissions): UIColor(parent) {
                override val level: String = "button"

                val background get() = App.color(getLevels("background"), default.accent.primary)
                val text get() = App.color(getLevels("text"), default.label.senary)
                val border get() = App.color(getLevels("border"), default.accent.primary)
            }
        }

        val success = Success(this)

        class Success(parent: Fanscan) : UIColor(parent) {
            override val level: String = "success"

            val icon get() = App.color(getLevels("icon"), default.accent.primary)
            val background get() = App.color(getLevels("background"), default.background.primary)
            val title get() = App.color(getLevels("title"), default.label.secondary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)

            val button = Button(this)

            class Button(parent: Success): UIColor(parent) {
                override val level: String = "button"

                val background get() = App.color(getLevels("background"), default.accent.primary)
                val text get() = App.color(getLevels("text"), default.label.senary)
                val border get() = App.color(getLevels("border"), default.accent.primary)
            }
        }
    }

    val eventPass = EventPass(this)
    class EventPass(parent: ThuziColor): ScreenColor(parent) {
        override val level: String = "eventpass"

        val header = Header(this)
        class Header(parent: EventPass) : UIColor(parent) {
            override val level: String = "header"

            val background get() = App.color(getLevels("background"), default.topBar.background)
            val title get() = App.color(getLevels("title"), default.topBar.title)
            val description get() = App.color(getLevels("description"), default.topBar.title)

        }

        val card = Card(this)
        class Card(parent: EventPass) : UIColor(parent) {
            override val level: String = "card"
            val border get() = App.color(getLevels("border"), default.fill.secondary)
            val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)

        }
    }

    val accountDeletion = AccountDeletion(this)
    class AccountDeletion(parent: ThuziColor): ScreenColor(parent) {
        override val level: String = "accountDeletion"

        val title get() = App.color(getLevels("title"), default.label.secondary)
        val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
        val footnote get() = App.color(getLevels("footnote"), default.label.tertiary)

        val primaryButton = PrimaryButton(this)
        class PrimaryButton(parent: AccountDeletion) : UIColor(parent) {
            override val level: String = "primaryButton"
            val background: Int get() = App.color(getLevels("background"), default.accent.primary)
            val text: Int get() = App.color(getLevels("text"), default.label.senary)
            val border: Int get() = App.color(getLevels("border"), default.accent.primary)
        }
    }

    val logout = Logout(this)

    class Logout(parent: ThuziColor): ScreenColor(parent) {
        override val level: String = "logout"

        val title @Composable get() = composeColor(leaf = "title") { default.label.secondary }
        val subtitle @Composable get() = composeColor(leaf = "subtitle") { default.label.secondary }

        val button = Button(this)
        class Button(parent: Logout) : UIColor(parent) {
            override val level: String = "button"
            val background @Composable get() = composeColor(leaf = "background") { default.accent.primary }
            val text @Composable get() = composeColor(leaf = "text") { default.label.senary }
        }
    }
}
