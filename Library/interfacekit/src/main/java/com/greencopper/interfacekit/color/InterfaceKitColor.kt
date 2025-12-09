package com.greencopper.interfacekit.color

import androidx.compose.runtime.Composable
import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.empty.ui.EmptyViewColors
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarColor
import com.greencopper.toolkit.App
import androidx.compose.ui.graphics.Color as ComposeColor

internal object InterfaceKitColor : UIColor() {
    override val level: String = "interfaceKit"
    val bottomBar: TabBar = TabBar(this)
    val webView: WebView = WebView(this)
    val sample: Sample = Sample(this)
    val imageWidget = ImageWidget(this)
    val buttonWidget = ButtonWidget(this)
    val titleSubtitleWidget = TitleSubtitleWidget(this)
    val textWidget = TextWidget(this)
    val linksCollectionWidget = LinksCollectionWidget(this)
    val cardAdWidget = CardAdWidget(this)
    val imageCollectionWidget = ImageCollectionWidget(this)
    val imageWithLabelWidget = ImageWithLabelWidget(this)
    val textOnImageWidget = TextOnImageWidget(this)
    val titleCounterWidget = TitleCounterWidget(this)
    val settingWidget = SettingWidget(this)
    val debugInfoWidget = DebugInfoWidget(this)
    val accountSummaryWidget = AccountSummaryWidget(this)
    val unregisteredAccountWidget = UnregisteredAccountWidget(this)
    val accountProfileWidget = AccountProfileWidget(this)
    val cardCollectionWidget = CardCollectionWidget(this)
    val bannerWidget = BannerWidget(this)
    val widgetCollection = WidgetCollection(this)
    val fullScreenMedia = FullScreenMedia(this)
    val mainActionCardOnboardingPage = MainActionCardOnboardingPage(this)
    val adOnboardingPage = AdOnboardingPage(this)
    val projectSwitcher = ProjectSwitcher(this)
    val projectSwitching = ProjectSwitching(this)
    val filters = FilterSelector(this)
    val search = Search(this)
    val inbox = Inbox(this)
    val interestsPicker = InterestsPicker(this)
    val countdownWidget = CountdownWidget(this)

    val navigationBar: Int get() = App.color(getLevels("navigationBar"), default.background.primary)

    class WidgetCollection(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "widgetCollection"
    }

    class ImageWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "imageWidget"

        val card = Card(this)

        class Card(parent: ImageWidget) : UIColor(parent) {
            override val level: String = "card"

            val border @Composable get() = composeColor("border") { default.fill.secondary }
            val shadow @Composable get() = composeColor("shadow") { default.fill.secondary }
        }
    }

    class ButtonWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "buttonWidget"

        val icon: Int get() = App.color(getLevels("icon"), default.label.senary)
        val text: Int get() = App.color(getLevels("text"), default.label.senary)
        val border: Int get() = App.color(getLevels("border"), default.accent.primary)
        val background: Int get() = App.color(getLevels("background"), default.accent.primary)
    }

    class TitleSubtitleWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "titleSubtitleWidget"

        val title: Int get() = App.color(getLevels("title"), default.label.primary)
        val subtitle: Int get() = App.color(getLevels("subtitle"), default.label.tertiary)
    }

    class TextWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "textWidget"

        val text: Int get() = App.color(getLevels("text"), default.label.secondary)
    }

    class LinksCollectionWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "linksCollectionWidget"

        val title: Int get() = App.color(getLevels("title"), default.label.primary)

        val link = Link(this)

        class Link(parent: LinksCollectionWidget) : UIColor(parent) {
            override val level: String = "link"

            val text: Int get() = App.color(getLevels("text"), default.label.secondary)
            val button = Button(this)

            class Button(parent: Link) : UIColor(parent) {
                override val level: String = "button"

                val icon: Int get() = App.color(getLevels("icon"), default.accent.primary)
                val border: Int get() = App.color(getLevels("border"), default.fill.secondary)
                val shadow: Int get() = App.color(getLevels("shadow"), default.fill.secondary)
                val background = Background(this)

                class Background(parent: Button) : PressableColor(parent) {
                    override val level: String = "background"

                    override val normalDefault: Color
                        get() = default.background.secondary
                    override val pressedDefault: Color
                        get() = default.fill.tertiary
                }
            }
        }
    }

    class CardAdWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "cardAdWidget"

        val card = Card(this)

        class Card(parent: CardAdWidget) : UIColor(parent) {
            override val level: String = "card"

            val border @Composable get() = composeColor("border") { default.fill.secondary }
            val shadow @Composable get() = composeColor("shadow") { default.fill.secondary }
        }
    }

    class ImageCollectionWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "imageCollectionWidget"

        val title: Int get() = App.color(getLevels("title"), default.label.primary)
        val item = Item(this)

        class Item(parent: ImageCollectionWidget) : UIColor(parent) {
            override val level: String = "item"

            val label: Int get() = App.color(getLevels("label"), default.label.secondary)
            val border: Int get() = App.color(getLevels("border"), default.fill.secondary)
            val shadow: Int get() = App.color(getLevels("shadow"), default.fill.secondary)
            val background get() = App.color(getLevels("background"), default.background.secondary)
        }
    }

    class ImageWithLabelWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "imageWithLabelWidget"

        val card = Card(this)
        val labels = Labels(this)

        class Card(parent: ImageWithLabelWidget) : UIColor(parent) {
            override val level: String = "card"

            val border: Int get() = App.color(getLevels("border"), default.fill.secondary)
            val shadow: Int get() = App.color(getLevels("shadow"), default.fill.secondary)
        }

        class Labels(parent: ImageWithLabelWidget) : UIColor(parent) {
            override val level: String = "labels"

            val background: Int get() = App.color(getLevels("background"), default.background.secondary)
            val title: Int get() = App.color(getLevels("title"), default.label.secondary)
            val body: Int get() = App.color(getLevels("body"), default.label.tertiary)
        }
    }

    class TextOnImageWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "textOnImageWidget"

        val card = Card(this)

        class Card(parent: TextOnImageWidget) : UIColor(parent) {
            override val level: String = "card"

            val border: Int get() = App.color(getLevels("border"), default.fill.secondary)
            val shadow: Int get() = App.color(getLevels("shadow"), default.fill.secondary)
        }
    }

    class TitleCounterWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "titleCounterWidget"

        class Background(parent: TitleCounterWidget) : UIColor(parent) {
            override val level: String = "background"

            val pressed: Int get() = App.color(getLevels("pressed"), default.fill.tertiary)
        }

        class Counter(parent: TitleCounterWidget) : UIColor(parent) {
            override val level: String = "counter"

            val empty = Empty(this)
            val nonEmpty = NonEmpty(this)

            class Empty(parent: Counter) : UIColor(parent) {
                override val level: String = "empty"

                val background: Int get() = App.color(getLevels("background"), default.fill.quinary)
                val label: Int get() = App.color(getLevels("label"), default.label.senary)
            }

            class NonEmpty(parent: Counter) : UIColor(parent) {
                override val level: String = "nonEmpty"

                val background: Int get() = App.color(getLevels("background"), default.accent.secondary)
                val label: Int get() = App.color(getLevels("label"), default.label.senary)
            }
        }

        val title: Int get() = App.color(getLevels("title"), default.label.primary)
        val icon: Int get() = App.color(getLevels("icon"), default.fill.quinary)
        val chevron: Int get() = App.color(getLevels("chevron"), default.fill.secondary)
        val separator: Int get() = App.color(getLevels("separator"), default.fill.secondary)
        val background = Background(this)
        val counter = Counter(this)
    }

    class SettingWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "settingWidget"

        class Background(parent: SettingWidget) : PressableColor(parent) {
            override val level: String = "background"

            override val normalDefault: Color
                get() = default.background.primary
            override val pressedDefault: Color
                get() = default.fill.tertiary
        }

        val background = Background(this)
        val title: Int get() = App.color(getLevels("title"), default.label.secondary)
        val subtitle: Int get() = App.color(getLevels("subtitle"), default.label.tertiary)
        val chevron: Int get() = App.color(getLevels("chevron"), default.accent.primary)
    }

    class DebugInfoWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "debugInfoWidget"

        val background: Int get() = App.color(getLevels("background"), default.background.primary)
        val text: Int get() = App.color(getLevels("text"), default.label.tertiary)
    }

    class AccountSummaryWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "accountSummaryWidget"

        class Button(parent: AccountSummaryWidget) : UIColor(parent) {
            override val level: String = "button"

            val background: Int get() = App.color(getLevels("background"), default.background.primary)
            val label: Int get() = App.color(getLevels("label"), default.label.secondary)
        }

        val button = Button(this)
        val background: Int get() = App.color(getLevels("background"), default.background.primary)
        val title: Int get() = App.color(getLevels("title"), default.label.secondary)
        val subtitle: Int get() = App.color(getLevels("subtitle"), default.label.tertiary)
        val initials: Int get() = App.color(getLevels("initials"), default.label.senary)
    }

    class UnregisteredAccountWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "UnregisteredAccountWidget"

        class Button(parent: UnregisteredAccountWidget) : UIColor(parent) {
            override val level: String = "button"

            val background: Int get() = App.color(getLevels("background"), default.accent.primary)
            val border: Int get() = App.color(getLevels("background"), default.accent.primary)
            val label: Int get() = App.color(getLevels("label"), default.label.senary)
        }

        val background: Int get() = App.color(getLevels("background"), default.background.primary)
        val text: Int get() = App.color(getLevels("text"), default.label.secondary)
        val icon: Int get() = App.color(getLevels("icon"), default.label.senary)
        val button = Button(this)
    }

    class AccountProfileWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "AccountProfileWidget"

        val background: Int get() = App.color(getLevels("background"), default.background.primary)
        val label: Int get() = App.color(getLevels("label"), default.label.primary)
        val text: Int get() = App.color(getLevels("text"), default.label.primary)
        val initials: Int get() = App.color(getLevels("initials"), default.label.senary)
    }

    class CardCollectionWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "cardsCollectionWidget"

        val title: ComposeColor @Composable get() = composeColor("title") { default.label.primary }
        val item: Item = Item(this)

        class Item(parent: CardCollectionWidget) : UIColor(parent) {
            override val level: String = "item"

            val label: ComposeColor @Composable get() = composeColor("label") { default.label.senary }
            val border: ComposeColor @Composable get() = composeColor("border") { default.fill.secondary }
            val icon: Icon = Icon(this)

            class Icon(parent: Item) : UIColor(parent) {
                override val level: String = "icon"

                val image: ComposeColor @Composable get() = composeColor("image") { default.accent.primary }
            }
        }
    }

    class BannerWidget(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "bannerWidget"

        val background: ComposeColor @Composable get() = composeColor("background") { default.background.secondary }
        val border: ComposeColor @Composable get() = composeColor("border") { default.fill.secondary }
        val title: ComposeColor @Composable get() = composeColor("title") { default.label.secondary }
        val subtitle: ComposeColor @Composable get() = composeColor("subtitle") { default.label.secondary }
        val button: Button = Button(this)

        class Button(parent: BannerWidget) : UIColor(parent) {
            override val level: String = "button"

            val text: ComposeColor @Composable get() = composeColor("text") { default.accent.primary }
            val icon: ComposeColor @Composable get() = composeColor("icon") { default.accent.primary }
        }
    }

    class TabBar(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "bottomBar"
        val background: Int get() = App.color(getLevels("background"), default.background.secondary)
        val backgroundComposable: ComposeColor @Composable get() = composeColor("background") { default.background.secondary }
        val item: Item = Item(this)

        class Item(parent: TabBar) : SelectableColorComposable(parent) {
            override val level: String = "item"
            override val normalDefault: () -> Color get() = { default.fill.quinary }
            override val selectedDefault: () -> Color get() = { default.accent.primary }
        }
    }

    class Sample(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val backgroundDefault: Color get() = default.background.secondary
        override val level: String = "sample"
        val text: Int get() = App.color(getLevels("text"), default.label.primary)
        val icon: Int get() = App.color(getLevels("icon"), default.fill.primary)
    }

    class WebView(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "webView"
    }

    class FullScreenMedia(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "fullScreenMedia"
    }

    class MainActionCardOnboardingPage(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "mainActionCardOnboardingPage"
        val card = Card(this)

        class Card(parent: MainActionCardOnboardingPage) : UIColor(parent) {
            override val level: String = "card"
            val background: Int get() = App.color(getLevels("background"), default.background.primary)
            val shadow: Int get() = App.color(getLevels("shadow"), default.fill.secondary)
            val title: Int get() = App.color(getLevels("title"), default.label.primary)
            val text: Int get() = App.color(getLevels("text"), default.label.secondary)
            val skip: Int get() = App.color(getLevels("skip"), default.label.secondary)
            val button = Button(this)
            val dots = Dots(this)

            class Button(parent: Card) : UIColor(parent) {
                override val level: String = "button"
                val background: Int get() = App.color(getLevels("background"), default.accent.primary)
                val text: Int get() = App.color(getLevels("text"), default.label.senary)
                val border: Int get() = App.color(getLevels("border"), default.accent.primary)
            }

            class Dots(parent: Card) : UIColor(parent) {
                override val level: String = "dots"
                val normal: Int get() = App.color(getLevels("normal"), default.fill.quinary)
                val selected: Int get() = App.color(getLevels("selected"), default.label.secondary)

                private val normalDefault: Color get() = default.label.tertiary
                val normalComposable: ComposeColor @Composable get() = composeColor("normal") { normalDefault }

                private val selectedDefault: Color get() = default.label.primary
                val selectedComposable: ComposeColor @Composable get() = composeColor("selected") { selectedDefault }
            }
        }
    }

    class AdOnboardingPage(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "adOnboardingPage"

        val closeButton = CloseButton(this)

        class CloseButton(parent: AdOnboardingPage) : UIColor(parent) {
            override val level: String = "closeButton"

            val background: Int get() = App.color(getLevels("background"), default.background.secondary)
            val icon: Int get() = App.color(getLevels("icon"), default.accent.primary)
        }
    }

    class ProjectSwitcher(parent: InterfaceKitColor) : ScreenColor(parent) {

        override val level: String = "projectSwitcher"

        val title: Int get() = App.color(getLevels("title"), default.label.secondary)
        val subtitle: Int get() = App.color(getLevels("subtitle"), default.label.tertiary)
        val swipeIndicator: Int get() = App.color(getLevels("swipeIndicator"), default.fill.primary)
        val footerTopShadow: Int get() = App.color(getLevels("footerTopShadow"), default.fill.secondary)
        val project = Project(this)
        val continueButton = ContinueButton(this)

        class Project(parent: ProjectSwitcher) : UIColor(parent) {

            override val level: String = "project"

            val background: Int get() = App.color(getLevels("background"), default.background.secondary)
            val title: Int get() = App.color(getLevels("title"), default.label.secondary)
            val checkbox: Int get() = App.color(getLevels("checkbox"), default.label.secondary)
            val separator: Int get() = App.color(getLevels("separator"), default.fill.secondary)
            val subtitle: Int get() = App.color(getLevels("subtitle"), default.label.tertiary)
        }

        class ContinueButton(parent: ProjectSwitcher) : UIColor(parent) {
            override val level: String = "continueButton"
            val background: Int get() = App.color(getLevels("background"), default.accent.primary)
            val text: Int get() = App.color(getLevels("text"), default.label.senary)
            val border: Int get() = App.color(getLevels("border"), default.accent.primary)
        }
    }

    class ProjectSwitching(parent: InterfaceKitColor) : ScreenColor(parent) {

        override val level: String = "projectSwitching"

        val activityIndicator: Int get() = App.color(getLevels("activityIndicator"), default.label.primary)
        val label: Int get() = App.color(getLevels("label"), default.label.tertiary)

    }

    class FilterSelector(parent: InterfaceKitColor) : UIColor(parent) {
        override val level: String = "filterSelector"

        val background: Int get() = App.color(getLevels("background"), default.background.primary)
        val title: Int get() = App.color(getLevels("title"), default.label.secondary)

        val checkBox = CheckBox(this)
        val actions = Actions(this)

        class CheckBox(parent: FilterSelector) : UIColor(parent) {
            override val level: String = "checkbox"

            val name: Int get() = App.color(getLevels("name"), default.label.secondary)

            val box = Box(this)

            class Box(parent: CheckBox) : SelectableColor(parent) {
                override val level: String = "box"
                override val normalDefault: Color get() = default.label.secondary
                override val selectedDefault: Color get() = default.accent.primary
            }
        }

        class Actions(parent: FilterSelector) : UIColor(parent) {
            override val level: String = "actions"

            val shadow: Int get() = App.color(getLevels("shadow"), default.fill.secondary)
            val background: Int get() = App.color(getLevels("background"), default.background.primary)

            val clearButton = ClearButton(this)
            val doneButton = DoneButton(this)

            class DoneButton(parent: Actions) : UIColor(parent) {
                override val level: String = "doneButton"

                val text: Int get() = App.color(getLevels("text"), default.label.senary)
                val background: Int get() = App.color(getLevels("background"), default.accent.primary)
                val border: Int get() = App.color(getLevels("border"), default.accent.primary)
            }

            class ClearButton(parent: Actions) : UIColor(parent) {
                override val level: String = "clearButton"

                val text: Int get() = App.color(getLevels("text"), default.label.primary)
            }
        }

    }

    class Search(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "search"

        val separator: Int get() = App.color(getLevels("separator"), default.fill.secondary)

        val header = Header(this)

        class Header(parent: Search) : UIColor(parent) {
            override val level: String = "header"

            val background: Int get() = App.color(getLevels("background"), default.topBar.background)
            val separator: Int get() = App.color(getLevels("separator"), default.fill.secondary)

            val searchField = SearchField(this)

            class SearchField(parent: Header) : UIColor(parent) {
                override val level: String = "searchField"

                val placeHolder: Int get() = App.color(getLevels("placeHolder"), default.topBar.title)
                val text: Int get() = App.color(getLevels("text"), default.topBar.title)
                val background: Int get() = App.color(getLevels("background"), default.fill.quinary)
                val button: Int get() = App.color(getLevels("button"), default.topBar.title)
            }

            val cancel = Cancel(this)

            class Cancel(parent: Header) : PressableColor(parent) {
                override val level: String = "cancel"
                override val normalDefault: Color get() = default.accent.secondary
                override val pressedDefault: Color get() = default.label.tertiary
            }
        }

        val titleSubtitleCell = TitleSubtitleCell(this)

        class TitleSubtitleCell(parent: Search) : UIColor(parent) {
            override val level: String = "titleSubtitleCell"

            val name: Int get() = App.color(getLevels("name"), default.label.secondary)
            val subtitle: Int get() = App.color(getLevels("subtitle"), default.label.tertiary)

            val image = Image(this)

            class Image(parent: TitleSubtitleCell) : UIColor(parent) {
                override val level: String = "image"

                val stroke: Int get() = App.color(getLevels("stroke"), default.fill.tertiary)
            }

            val background = Background(this)

            class Background(parent: TitleSubtitleCell) : PressableColor(parent) {
                override val level: String = "background"
                override val normalDefault: Color get() = default.background.primary
                override val pressedDefault: Color get() = default.fill.tertiary
            }
        }

        val empty = EmptyViewColors(this)
    }

    class Inbox(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "inbox"

        val header = Header(this)

        class Header(parent: Inbox) : UIColor(parent) {
            override val level: String = "header"

            val background: Int get() = App.color(getLevels("background"), default.background.primary)
            val separator: Int get() = App.color(getLevels("separator"), default.fill.secondary)
            val text: Int get() = App.color(getLevels("text"), default.label.secondary)

        }

        val item = Item(this)

        class Item(parent: Inbox) : PressableColor(parent) {
            override val level: String = "item"

            override val normalDefault: Color by lazy { default.background.primary }
            override val pressedDefault: Color by lazy { default.fill.tertiary }

            val text: Int get() = App.color(getLevels("text"), default.label.tertiary)
            val title: Int get() = App.color(getLevels("title"), default.label.primary)
            val date: Int get() = App.color(getLevels("date"), default.label.tertiary)
            val arrow: Int get() = App.color(getLevels("arrow"), default.accent.primary)
        }

        val empty = EmptyViewColors(this)
        val loader: Int get() = App.color(getLevels("loader"), default.label.secondary)
    }

    val list = List(this)

    class List(parent: InterfaceKitColor) : ScreenColor(parent) {
        override val level: String = "list"
        val grid = Grid(this)
        val table = Table(this)

        class Grid(parent: List) : UIColor(parent) {
            override val level: String = "grid"

            val item = Item(this)

            class Item(parent: Grid) : UIColor(parent) {
                override val level: String = "item"

                val label @Composable get() = composeColor("label") { default.label.senary }
                val border @Composable get() = composeColor("border") { default.fill.secondary }
                val shadow @Composable get() = composeColor("shadow") { default.fill.secondary }
                val background @Composable get() = composeColor("background") { default.background.secondary }

                val favoriteButton = FavoriteButton(this)

                class FavoriteButton(parent: Item) : UIColor(parent) {
                    override val level: String = "favoriteButton"

                    val background @Composable get() = composeColor("background") { default.background.secondary }
                    val icon @Composable get() = composeColor("icon") { default.accent.primary }
                }

            }
        }

        class Table(parent: List) : UIColor(parent) {
            override val level: String = "table"

            val item = Item(this)

            class Item(parent: Table) : UIColor(parent) {
                override val level: String = "item"
                val title @Composable get() = composeColor("title") { default.label.primary }
                val subtitle @Composable get() = composeColor("subtitle") { default.label.tertiary }
                val favoriteIcon @Composable get() = composeColor("favoriteIcon") { default.accent.primary }
                val separator @Composable get() = composeColor("separator") { default.fill.primary }
                val background = Background(this)
                val image = Image(this)

                class Background(parent: Item) : PressableColorComposable(parent) {
                    override val level: String = "background"
                    override val normalDefault: () -> Color = { default.background.primary }
                    override val pressedDefault: () -> Color = { default.fill.tertiary }
                }

                class Image(parent: Item) : UIColor(parent) {
                    override val level: String = "image"
                    val stroke @Composable get() = composeColor("stroke") { default.fill.tertiary }
                }
            }
        }

        val empty = EmptyViewColors(this)

        val filters = FilteringBarColor(this)
    }

    class InterestsPicker(parent: InterfaceKitColor) : ScreenColor(parent) {

        override val level: String = "interestsPicker"

        val title @Composable get() = composeColor(leaf = "title") { default.label.primary }
        val subtitle @Composable get() = composeColor(leaf = "subtitle") { default.label.secondary }
        val dots = Dots(this)
        val item = Item(this)
        val confirmButton = ConfirmButton(this)

        class Dots(parent: InterestsPicker): SelectableColorComposable(parent) {
            override val level: String = "dots"
            override val normalDefault: () -> Color get() = { default.label.tertiary }
            override val selectedDefault: () -> Color get() = { default.label.primary }
        }

        class Item(parent: InterestsPicker) : UIColor(parent) {
            override val level: String = "item"

            val background = Background(this)
            val label = Label(this)
            val border = Border(this)

            class Background(parent: Item) : SelectableColorComposable(parent) {
                override val level: String = "background"
                override val normalDefault: () -> Color get() = { default.background.secondary }
                override val selectedDefault: () -> Color get() = { default.accent.secondary }
            }

            class Label(parent: Item) : SelectableColorComposable(parent) {
                override val level: String = "label"
                override val normalDefault: () -> Color get() = { default.label.secondary }
                override val selectedDefault: () -> Color get() = { default.label.senary }
            }

            class Border(parent: Item) : SelectableColorComposable(parent) {
                override val level: String = "border"
                override val normalDefault: () -> Color get() = { default.fill.secondary }
                override val selectedDefault: () -> Color get() = { default.accent.secondary }
            }
        }

        class ConfirmButton(parent: InterestsPicker) : UIColor(parent) {

            override val level: String = "confirmButton"

            val text @Composable get() = composeColor(leaf = "text") { default.label.senary }
            val border @Composable get() = composeColor(leaf = "border") { default.accent.primary }
            val background @Composable get() = composeColor(leaf = "background") { default.accent.primary }
        }
    }

    class CountdownWidget(parent: InterfaceKitColor) : ScreenColor(parent) {

        override val level: String = "countdownWidget"

        val border: Int get() = App.color(getLevels("border"), default.fill.secondary)
    }
}
