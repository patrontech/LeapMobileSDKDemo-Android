package com.greencopper.thuzi.style

import androidx.compose.runtime.Composable
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.textstyle.subsystem.ScreenTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.UITextStyle

internal object ThuziTextStyle: UITextStyle() {
    override val level: String = "thuzi"

    val fanscan = FanScan(this)
    class FanScan(parent: ThuziTextStyle) : UITextStyle(parent) {
        override val level: String = "fanScan"

        val scanner = Scanner(this)

        class Scanner(parent: FanScan) : ScreenTextStyle(parent) {
            override val level: String = "scanner"

            val title get() = toIKFont("title", IKFont.TextStyle.titleXL)
            val instructions get() = toIKFont("subtitle", IKFont.TextStyle.bodyL)
        }

        val permissions = Permissions(this)
        class Permissions(parent: FanScan) : ScreenTextStyle(parent) {
            override val level: String = "permissions"

            val button get() = toIKFont("button", IKFont.TextStyle.headlineM)
            val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.titleS)
            val description get() = toIKFont("description", IKFont.TextStyle.bodyM)
        }

        val success = Success(this)
        class Success(parent: FanScan) : UITextStyle(parent) {
            override val level: String = "success"

            val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyM)
            val button get() = toIKFont("button", IKFont.TextStyle.headlineM)
        }
    }

    val badges = Badges(this)
    class Badges(parent: ThuziTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "badges"

        val header = Header(this)
        class Header(parent: Badges) : UITextStyle(parent) {
            override val level: String = "header"

            val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            val description get() = toIKFont("description", IKFont.TextStyle.bodyL)
            val number get() = toIKFont("number", IKFont.TextStyle.headlineL)
        }

        val card = Card(this)
        class Card(parent: Badges) : UITextStyle(parent) {
            override val level: String = "card"

            val label = Label(this)
            class Label(parent: Card) : UITextStyle(parent) {
                override val level: String = "label"

                val locked get() = toIKFont("locked", IKFont.TextStyle.headlineS)
                val unlocked get() = toIKFont("unlocked", IKFont.TextStyle.headlineS)
            }
        }

        val detail = Detail(this)
        class Detail(parent: Badges) : UITextStyle(parent) {
            override val level: String = "detail"

            val title get() = toIKFont("title", IKFont.TextStyle.titleS)
            val description get() = toIKFont("description", IKFont.TextStyle.bodyL)
        }
    }

    val eventPass = EventPass(this)
    class EventPass(parent: ThuziTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "eventPass"

        val header = Header(this)
        class Header(parent: EventPass) : UITextStyle(parent) {
            override val level: String = "header"

            val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            val description get() = toIKFont("description", IKFont.TextStyle.bodyL)
        }
    }

    val accountDeletion = AccountDeletion(this)
    class AccountDeletion(parent: ThuziTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "accountDeletion"

        val title get() = toIKFont("title", IKFont.TextStyle.titleL)
        val subtitle get() = toIKFont("description", IKFont.TextStyle.titleS)
        val footnote get() = toIKFont("footnote", IKFont.TextStyle.bodyM)
        val primaryButton get() = toIKFont("primaryButton", IKFont.TextStyle.bodyM)
    }

    val logout = Logout(this)

    class Logout(parent: ThuziTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "logout"

        val title @Composable get() = composeIKFont("title", IKFont.TextStyle.titleL)
        val subtitle @Composable get() = composeIKFont("description", IKFont.TextStyle.bodyM)
        val button @Composable get() = composeIKFont("button", IKFont.TextStyle.bodyM)
    }
}