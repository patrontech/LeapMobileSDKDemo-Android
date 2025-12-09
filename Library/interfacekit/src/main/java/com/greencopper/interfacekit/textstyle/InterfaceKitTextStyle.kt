package com.greencopper.interfacekit.textstyle

import androidx.compose.runtime.Composable
import com.greencopper.interfacekit.empty.ui.EmptyViewTextStyles
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.IKFont
import com.greencopper.interfacekit.textstyle.subsystem.IKFont.TextStyle
import com.greencopper.interfacekit.textstyle.subsystem.ScreenTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.SelectableTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.UITextStyle

internal object InterfaceKitTextStyle : UITextStyle() {
    override val level: String = "interfaceKit"

    val sample = Sample(this)

    class Sample(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "sample"

        val text: IKFont get() = toIKFont("text")

        val largeTitle: IKFont get() = toIKFont("largeTitle", TextStyle.largeTitle, text)
        val titleXL: IKFont get() = toIKFont("titleXL", TextStyle.titleXL, text)
        val titleL: IKFont get() = toIKFont("titleL", TextStyle.titleL, text)
        val titleM: IKFont get() = toIKFont("titleM", TextStyle.titleM, text)
        val titleS: IKFont get() = toIKFont("titleS", TextStyle.titleS, text)
        val titleXS: IKFont get() = toIKFont("titleXS", TextStyle.titleXS, text)
        val headlineL: IKFont get() = toIKFont("headlineL", TextStyle.headlineL, text)
        val headlineM: IKFont get() = toIKFont("headlineM", TextStyle.headlineM, text)
        val headlineS: IKFont get() = toIKFont("headlineS", TextStyle.headlineS, text)
        val bodyXL: IKFont get() = toIKFont("bodyXL", TextStyle.bodyXL, text)
        val bodyL: IKFont get() = toIKFont("bodyL", TextStyle.bodyL, text)
        val bodyM: IKFont get() = toIKFont("bodyM", TextStyle.bodyM, text)
        val bodyS: IKFont get() = toIKFont("bodyS", TextStyle.bodyS, text)
        val bodyXS: IKFont get() = toIKFont("bodyXS", TextStyle.bodyXS, text)
        val footnoteM: IKFont get() = toIKFont("footnoteM", TextStyle.footnoteM, text)
        val footnoteS: IKFont get() = toIKFont("footnoteS", TextStyle.footnoteS, text)
        val captionL: IKFont get() = toIKFont("captionL", TextStyle.captionL, text)
        val captionS: IKFont get() = toIKFont("captionS", TextStyle.captionS, text)
    }

    val widgetCollection = WidgetCollection(this)

    class WidgetCollection(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "widgetCollection"
    }

    val buttonWidget = ButtonWidget(this)

    class ButtonWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "buttonWidget"

        val text get() = toIKFont("text", TextStyle.headlineM)
    }

    val titleSubtitleWidget = TitleSubtitleWidget(this)

    class TitleSubtitleWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "titleSubtitleWidget"

        val title get() = toIKFont("title", TextStyle.titleM)
        val subtitle get() = toIKFont("subtitle", TextStyle.bodyL)
    }

    val textWidget = TextWidget(this)

    class TextWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "textWidget"

        val text get() = toIKFont("text", TextStyle.bodyL)
    }

    val linksCollectionWidget = LinksCollectionWidget(this)

    class LinksCollectionWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "linksCollectionWidget"

        val title get() = toIKFont("title", TextStyle.titleM)

        val link = Link(this)

        class Link(parent: LinksCollectionWidget) : UITextStyle(parent) {
            override val level: String = "link"

            val text get() = toIKFont("text", TextStyle.bodyXS)
        }
    }

    val imageCollectionWidget = ImageCollectionWidget(this)

    class ImageCollectionWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "imageCollectionWidget"

        val title get() = toIKFont("title", TextStyle.titleM)
        val image = Image(this)

        class Image(parent: ImageCollectionWidget) : UITextStyle(parent) {
            override val level: String = "image"

            val label get() = toIKFont("label", TextStyle.bodyXS)
        }
    }

    val imageWithLabelWidget = ImageWithLabelWidget(this)

    class ImageWithLabelWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "imageWithLabelWidget"

        val title get() = toIKFont("title", TextStyle.titleS)
        val body get() = toIKFont("body", TextStyle.bodyXS)
    }

    val textOnImageWidget = TextOnImageWidget(this)

    class TextOnImageWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "textOnImageWidget"

        val title get() = toIKFont("title", TextStyle.titleXL)
        val body get() = toIKFont("body", TextStyle.bodyM)
    }

    val titleCounterWidget = TitleCounterWidget(this)

    class TitleCounterWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "titleCounterWidget"

        val title get() = toIKFont("title", TextStyle.headlineM)
        val counter get() = toIKFont("counter", TextStyle.headlineM)
    }

    val settingWidget = SettingWidget(this)

    class SettingWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "SettingWidget"

        val title get() = toIKFont("title", TextStyle.headlineM)
        val subtitle get() = toIKFont("counter", TextStyle.bodyL)
    }

    val debugInfoWidget = DebugInfoWidget(this)

    class DebugInfoWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "settingWidget"

        val text get() = toIKFont("text", TextStyle.bodyL)
    }

    val accountSummaryWidget = AccountSummaryWidget(this)

    class AccountSummaryWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "accountSummaryWidget"

        val title get() = toIKFont("title", TextStyle.titleL)
        val subtitle get() = toIKFont("subtitle", TextStyle.bodyM)
        val button = Button(this)

        class Button(parent: AccountSummaryWidget) : UITextStyle(parent) {
            override val level: String = "button"

            val label get() = toIKFont("label", TextStyle.bodyM)
        }
    }

    val unregisteredAccountWidget = UnregisteredAccountWidget(this)

    class UnregisteredAccountWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "unregisteredAccountWidget"

        class Button(parent: UnregisteredAccountWidget) : UITextStyle(parent) {
            override val level: String = "button"
            val label get() = toIKFont("label", TextStyle.headlineM)
        }

        val text get() = toIKFont("text", TextStyle.bodyM)
        val button = Button(this)
    }

    val accountProfileWidget = AccountProfileWidget(this)

    class AccountProfileWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "accountProfileWidget"

        val text get() = toIKFont("text", TextStyle.headlineM)
        val label get() = toIKFont("label", TextStyle.bodyXS)
    }

    val countdownWidget = CountdownWidget(this)

    class CountdownWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "countdownWidget"

        val title get() = toIKFont("text", TextStyle.headlineM)
        val subtitle get() = toIKFont("text", TextStyle.headlineM)
        val endDate get() = toIKFont("text", TextStyle.headlineM)
        val indicator get() = toIKFont("text", TextStyle.titleL)
        val indicatorLabel get() = toIKFont("text", TextStyle.headlineL)
    }

    val cardCollectionWidget = CardCollectionWidget(this)

    class CardCollectionWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "cardsCollectionWidget"

        val title @Composable get() = composeIKFont("title", TextStyle.titleM)
        val item = Item(this)

        class Item(parent: CardCollectionWidget) : UITextStyle(parent) {
            override val level: String = "item"

            val label @Composable get() = composeIKFont("label", TextStyle.captionS)
        }
    }

    val bannerWidget = BannerWidget(this)

    class BannerWidget(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "bannerWidget"

        val title @Composable get() = composeIKFont("title", TextStyle.headlineL)
        val subtitle @Composable get() = composeIKFont("subtitle", TextStyle.bodyS)
        val button = Button(this)

        class Button(parent: BannerWidget) : UITextStyle(parent) {
            override val level: String = "button"

            val text @Composable get() = composeIKFont("text", TextStyle.captionS)
        }
    }

    val tabBar = TabBar(this)

    class TabBar(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "bottomBar"
        val item: Item = Item(this)

        class Item(parent: TabBar) : UITextStyle(parent) {
            override val level: String = "item"
            val normal @Composable get() = composeIKFont("normal", TextStyle.footnoteS)
            val selected @Composable get() = composeIKFont("selected", TextStyle.footnoteM)
        }
    }

    val webView = WebView(this)

    class WebView(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "webview"
    }

    val inbox = Inbox(this)

    class Inbox(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "inbox"

        val header = Header(this)

        class Header(parent: Inbox) : UITextStyle(parent) {
            override val level: String = "header"

            val text get() = toIKFont("text", TextStyle.headlineL)

        }

        val item = Item(this)

        class Item(parent: Inbox) : UITextStyle(parent) {
            override val level: String = "item"

            val date get() = toIKFont("date", TextStyle.bodyXS)
            val title get() = toIKFont("title", TextStyle.headlineM)
            val text get() = toIKFont("text", TextStyle.bodyS)
        }

        val empty = EmptyViewTextStyles(this)
    }

    val filterSelector = FilterSelector(this)

    class FilterSelector(parent: InterfaceKitTextStyle) : UITextStyle(parent) {
        override val level: String = "filterSelector"

        val title get() = toIKFont("title", TextStyle.titleM)

        val checkBox = CheckBox(this)

        class CheckBox(parent: FilterSelector) : UITextStyle(parent) {
            override val level: String = "checkbox"

            val name get() = toIKFont("name", TextStyle.bodyXL)
        }

        val actions = Actions(this)

        class Actions(parent: FilterSelector) : UITextStyle(parent) {
            override val level: String = "actions"

            val doneButton get() = toIKFont("doneButton", TextStyle.bodyM)
            val clearButton get() = toIKFont("clearButton", TextStyle.headlineL)
        }
    }

    val projectSwitcher = ProjectSwitcher(this)

    class ProjectSwitcher(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {

        override val level: String = "projectSwitcher"

        val title get() = toIKFont("title", TextStyle.titleXL)
        val subtitle get() = toIKFont("subtitle", TextStyle.bodyM)
        val continueButton get() = toIKFont("continueButton", TextStyle.bodyM)
        val project = Project(this)

        class Project(parent: ProjectSwitcher) : UITextStyle(parent) {
            override val level: String = "project"

            val title get() = toIKFont("title", TextStyle.headlineL)
            val subtitle get() = toIKFont("subtitle", TextStyle.headlineS)
        }
    }

    val projectSwitching = ProjectSwitching(this)

    class ProjectSwitching(parent: InterfaceKitTextStyle) : UITextStyle(parent) {

        override val level: String = "projectSwitching"

        val label get() = toIKFont("label", TextStyle.bodyM)
    }

    val mainActionCardOnboardingPage = MainActionCardOnboardingPage(this)

    class MainActionCardOnboardingPage(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "mainActionCardOnboardingPage"
        val card = Card(this)

        class Card(parent: MainActionCardOnboardingPage) : UITextStyle(parent) {
            override val level: String = "card"
            val title get() = toIKFont("title", TextStyle.titleXL)
            val text get() = toIKFont("text", TextStyle.bodyL)
            val button get() = toIKFont("button", TextStyle.bodyM)
            val skip get() = toIKFont("skip", TextStyle.headlineL)
        }
    }

    val search = Search(this)

    class Search(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "search"

        val header = Header(this)

        class Header(parent: Search) : UITextStyle(parent) {
            override val level: String = "header"

            val cancel get() = toIKFont("cancel", TextStyle.bodyL)

            val searchField = SearchField(this)

            class SearchField(parent: Header) : UITextStyle(parent) {
                override val level: String = "searchField"

                val placeHolder get() = toIKFont("placeHolder", TextStyle.bodyM)
                val text get() = toIKFont("text", TextStyle.bodyM)
            }
        }

        val titleSubtitleCell = TitleSubtitleCell(this)

        class TitleSubtitleCell(parent: Search) : UITextStyle(parent) {
            override val level: String = "titleSubtitleCell"

            val name get() = toIKFont("name", TextStyle.headlineM)
            val subtitle get() = toIKFont("subtitle", TextStyle.headlineS)
        }

        val empty = EmptyViewTextStyles(this)
    }

    val tags = Tags(this)

    class Tags(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "tags"

        val label get() = toIKFont("label", TextStyle.headlineS)
    }

    val list = List(this)

    class List(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "list"

        val filters = FilteringBarTextStyle(this)
        val grid = Grid(this)
        val table = Table(this)
        val empty = EmptyViewTextStyles(this)

        class Grid(parent: List) : UITextStyle(parent) {
            override val level: String = "grid"

            val label @Composable get() = composeIKFont("label", TextStyle.headlineL)
        }

        class Table(parent: List) : UITextStyle(parent) {
            override val level: String = "table"

            val title @Composable get() = composeIKFont("title", TextStyle.headlineM)
            val subtitle @Composable get() = composeIKFont("subtitle", TextStyle.headlineS)
        }
    }

    val interestsPicker = InterestsPicker(this)

    class InterestsPicker(parent: InterfaceKitTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "interestsPicker"

        val title @Composable get() = composeIKFont("title", TextStyle.titleXL)
        val subtitle @Composable get() = composeIKFont("subtitle", TextStyle.bodyL)
        val confirmButton @Composable get() = composeIKFont("confirmButton", TextStyle.bodyM)

        val item = Item(this)

        class Item(parent: InterestsPicker) : SelectableTextStyle(parent) {
            override val level: String = "item"

            override val normalDefault: TextStyle get() = TextStyle.headlineM
            override val selectedDefault: TextStyle get() = TextStyle.headlineM
        }
    }
}
