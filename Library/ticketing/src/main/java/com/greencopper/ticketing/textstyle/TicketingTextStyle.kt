package com.greencopper.ticketing.textstyle

import com.greencopper.interfacekit.textstyle.subsystem.*

internal object TicketingTextStyle : UITextStyle() {
    override val level: String = "ticketing"

    val ticketsScan = TicketsScan(this)

    class TicketsScan(parent: TicketingTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "ticketsScan"

        val title get() = toIKFont("title", IKFont.TextStyle.titleL)
        val reload get() = toIKFont("reload", IKFont.TextStyle.titleS)

        val noTickets = NoTickets(this)

        class NoTickets(parent: ScreenTextStyle) : ScreenTextStyle(parent) {
            override val level: String = "noTickets"

            val title get() = toIKFont("title", IKFont.TextStyle.headlineL)
            val subtitle get() = toIKFont("title", IKFont.TextStyle.bodyS)
        }
    }

    val showclixLogin = ShowclixLogin(this)

    class ShowclixLogin(parent: TicketingTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "showclixLogin"

        val button get() = toIKFont("button", IKFont.TextStyle.headlineM)
        val title get() = toIKFont("title", IKFont.TextStyle.titleL)
        val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyM)
        val email get() = toIKFont("email", IKFont.TextStyle.titleS)
        val changeEmail get() = toIKFont("changeEmail", IKFont.TextStyle.headlineM)

        val emailTextField = EmailTextField(this)

        class EmailTextField(parent: ShowclixLogin) : UITextStyle(parent) {
            override val level: String = "emailTextField"

            val label get() = toIKFont("label", IKFont.TextStyle.bodyM)
            val error get() = toIKFont("error", IKFont.TextStyle.bodyS)
        }
    }
}
