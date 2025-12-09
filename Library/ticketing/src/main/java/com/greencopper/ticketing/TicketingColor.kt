package com.greencopper.ticketing

import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.*
import com.greencopper.toolkit.App

internal object TicketingColor : UIColor() {
    override val level: String = "ticketing"

    val ticketsScan = TicketsScan(this)
    val showclixLogin = ShowclixLogin(this)

    class TicketsScan(parent: TicketingColor) : ScreenColor(parent) {
        override val level: String = "ticketsScan"

        val header = Header(this)
        val noTickets = NoTickets(this)
        val tickets = Tickets(this)

        class Header(parent: TicketsScan) : UIColor(parent) {
            override val level: String = "header"

            val background get() = App.color(getLevels("background"), default.topBar.background)
            val title get() = App.color(getLevels("title"), default.topBar.title)
        }

        class NoTickets(parent: TicketsScan) : UIColor(parent) {
            override val level: String = "noTickets"

            val reload get() = Reload(this)
            val card get() = Card(this)

            class Reload(parent: NoTickets) : UIColor(parent) {
                override val level: String = "reload"

                val label get() = App.color(getLevels("label"), default.topBar.title)
                val icon get() = App.color(getLevels("icon"), default.topBar.item)
            }

            class Card(parent: NoTickets) : UIColor(parent) {
                override val level: String = "card"

                val border get() = App.color(getLevels("border"), default.fill.secondary)
            }
        }

        class Tickets(parent: TicketsScan) : UIColor(parent) {
            override val level: String = "tickets"

            val ticket = Ticket(this)
            val pageIndicator = PageIndicator(this)

            class Ticket(parent: Tickets) : UIColor(parent) {
                override val level: String = "ticket"

                val qrCodeCard = QrCodeCard(this)
                val primaryInfoCard = PrimaryInfoCard(this)
                val secondaryInfoCard = SecondaryInfoCard(this)

                class QrCodeCard(parent: Ticket) : UIColor(parent) {
                    override val level: String = "qrCodeCard"

                    val border get() = App.color(getLevels("border"), default.fill.secondary)
                    val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
                }

                class PrimaryInfoCard(parent: Ticket) : UIColor(parent) {
                    override val level: String = "primaryInfoCard"

                    val border get() = App.color(getLevels("border"), default.fill.secondary)
                }

                class SecondaryInfoCard(parent: Ticket) : UIColor(parent) {
                    override val level: String = "secondaryInfoCard"

                    val border get() = App.color(getLevels("border"), default.fill.secondary)
                }
            }

            class PageIndicator(parent: Tickets) : SelectableColor(parent) {
                override val level: String = "pageIndicator"
                override val normalDefault: Color get() = default.label.tertiary
                override val selectedDefault: Color get() = default.label.primary
            }
        }
    }

    class ShowclixLogin(parent: TicketingColor) : ScreenColor(parent) {
        override val level: String = "showclixLogin"

        val title get() = App.color(getLevels("title"), default.label.secondary)
        val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
        val email get() = App.color(getLevels("email"), default.label.secondary)
        val changeEmail get() = App.color(getLevels("changeEmail"), default.label.secondary)
        val loader get() = App.color(getLevels("loader"), default.label.tertiary)

        val button = Button(this)

        class Button(parent: ShowclixLogin) : UIColor(parent) {
            override val level: String = "button"

            val background get() = App.color(getLevels("label"), default.accent.primary)
            val label get() = App.color(getLevels("label"), default.label.senary)
        }

        val emailTextField = EmailTextField(this)

        class EmailTextField(parent: ShowclixLogin) : UIColor(parent) {
            override val level: String = "emailTextField"

            val label get() = App.color(getLevels("label"), default.label.primary)
            val icon get() = App.color(getLevels("icon"), default.accent.primary)
            val error get() = App.color(getLevels("error"), default.result.error)
            val background get() = App.color(getLevels("background"), default.background.primary)
            val border get() = App.color(getLevels("background"), default.fill.primary)
        }
    }
}
