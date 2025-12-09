package com.greencopper.event.colors

import com.greencopper.interfacekit.color
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.empty.ui.EmptyViewColors
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarColor
import com.greencopper.interfacekit.tags.ui.TagColor
import com.greencopper.toolkit.App
import com.greencopper.interfacekit.lists.ui.EmptyViewColors as IEmptyViewColors

internal object EventColor : UIColor() {
    override val level: String = "event"

    val activitiesList = ActivitiesList(this)

    class ActivitiesList(parent: EventColor) : ScreenColor(parent) {
        override val level: String = "activitiesList"
        val separator get() = App.color(getLevels("separator"), default.fill.primary)

        val cell = Cell(this)
        val filters = FilteringBarColor(this)
        val empty = Empty(this)

        class Empty(parent: ActivitiesList) : UIColor(parent), IEmptyViewColors {
            override val level: String = "empty"
            override val title get() = App.color(getLevels("title"), default.label.primary)
            override val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
        }

        class Cell(parent: ActivitiesList) : UIColor(parent) {
            override val level: String = "cell"
            val name get() = App.color(getLevels("name"), default.label.secondary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
            val myActivityIcon
                get() = App.color(
                    getLevels("myActivityIcon"),
                    default.accent.primary
                )

            val background = Background(this)

            class Background(parent: Cell) : PressableColor(parent) {
                override val level: String = "background"
                override val normalDefault: Color get() = default.background.primary
                override val pressedDefault: Color get() = default.fill.tertiary
            }
        }
    }

    val schedule = ScheduleList(this)

    class ScheduleList(parent: EventColor) : ScreenColor(parent) {
        override val level: String = "schedule"
        val datepicker = DatePicker(this)

        class DatePicker(parent: ScheduleList) : UIColor(parent) {

            override val level: String = "datepicker"
            val background get() = App.color(getLevels("background"), default.topBar.background)
            val selectedItem = SelectedItem(this)

            class SelectedItem(parent: DatePicker) : UIColor(parent) {
                override val level: String = "selectedItem"
                val background get() = App.color(getLevels("background"), default.topBar.item)
            }

            val label = Label(this)

            class Label(parent: DatePicker) : SelectableColor(parent) {
                override val level: String = "label"
                override val normalDefault get() = default.topBar.title
                override val selectedDefault get() = default.topBar.background
            }
        }

        val card = Card(this)

        class Card(parent: ScheduleList) : UIColor(parent) {
            override val level: String = "card"

            val background = Background(this)

            class Background(parent: Card) : PressableColor(parent) {
                override val level: String = "background"
                override val normalDefault: Color get() = default.background.secondary
                override val pressedDefault: Color get() = default.fill.tertiary
            }

            val image = Image(this)

            class Image(parent: Card) : UIColor(parent) {
                override val level: String = "image"
                val stroke get() = App.color(getLevels("stroke"), default.fill.tertiary)
            }

            val mySchedule = MySchedule(this)

            class MySchedule(parent: Card) : SelectableColor(parent) {
                override val level: String = "mySchedule"
                override val selectedDefault: Color get() = default.accent.primary
                override val normalDefault: Color get() = default.accent.primary
            }

            val border get() = App.color(getLevels("border"), default.fill.secondary)
            val shadow get() = App.color(getLevels("shadow"), default.label.secondary)
            val name get() = App.color(getLevels("name"), default.label.secondary)
            val hours get() = App.color(getLevels("hours"), default.label.tertiary)
            val stage get() = App.color(getLevels("stage"), default.label.quaternary)
        }

        val timeSeparator = TimeSeparator(this)

        class TimeSeparator(parent: ScheduleList) : UIColor(parent) {
            override val level: String = "timeSeparator"
            val leftShape get() = App.color(getLevels("leftShape"), default.fill.quaternary)
            val label get() = App.color(getLevels("label"), default.label.quinary)
            val dash get() = App.color(getLevels("dash"), default.fill.primary)
        }

        val daySeparator get() = App.color(getLevels("daySeparator"), default.label.secondary)

        val nextButton = NextButton(this)

        class NextButton(parent: ScheduleList) : UIColor(parent) {
            override val level: String = "nextButton"

            val text get() = App.color(getLevels("text"), default.label.senary)
            val background get() = App.color(getLevels("background"), default.accent.primary)
            val border get() = App.color(getLevels("border"), default.accent.primary)
        }

        val oneDay = OneDay(this)

        class OneDay(parent: ScheduleList) : UIColor(parent) {
            override val level: String = "oneDay"
            val background get() = App.color(getLevels("background"), default.topBar.background)
            val label get() = App.color(getLevels("label"), default.topBar.title)
        }

        val empty = EmptyViewColors(this)

        val filters = FilteringBarColor(this)

        val reminders = Reminders(this)

        class Reminders(parent: ScheduleList) : UIColor(parent) {
            override val level: String = "reminders"

            val background get() = App.color(getLevels("background"), default.background.primary)
            val title get() = App.color(getLevels("title"), default.label.primary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.secondary)

            val option = Option(this)

            class Option(parent: Reminders) : UIColor(parent) {
                override val level: String = "option"

                val text get() = App.color(getLevels("text"), default.label.primary)
                val radioButton
                    get() = App.color(
                        getLevels("radioButton"),
                        default.accent.secondary
                    )
                val radioButtonBorder
                    get() = App.color(
                        getLevels("radioButtonBorder"),
                        default.label.secondary
                    )
            }
        }

        val timeline = Timeline(this)

        class Timeline(parent: ScheduleList) : UIColor(parent) {
            override val level: String = "timeline"

            val background = Background(this)
            val currentTimeIndicator
                get() = App.color(
                    getLevels("currentTimeIndicator"),
                    default.accent.secondary
                )
            val timeIndicator get() = App.color(getLevels("timeIndicator"), default.fill.primary)
            val stage = Stage(this)
            val myScheduleCard = MyScheduleCard(this)
            val card = Card(this)
            val hourLine = HourLine(this)

            class Background(parent: Timeline) : UIColor(parent) {
                override val level: String = "background"

                val future get() = App.color(getLevels("future"), default.background.primary)
                val past get() = App.color(getLevels("past"), default.fill.tertiary)
            }

            class Stage(parent: Timeline) : UIColor(parent) {
                override val level: String = "stage"

                val title get() = App.color(getLevels("title"), default.label.primary)
                val borders get() = App.color(getLevels("borders"), default.fill.secondary)
                val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
                val background
                    get() = App.color(
                        getLevels("background"),
                        default.background.primary
                    )
            }

            class MyScheduleCard(parent: Timeline) : UIColor(parent) {
                override val level: String = "myScheduleCard"

                val background = Background(this)
                val title get() = App.color(getLevels("title"), default.label.senary)
                val subtitle get() = App.color(getLevels("subtitle"), default.label.senary)
                val border get() = App.color(getLevels("border"), default.fill.tertiary)
                val myScheduleIcon
                    get() = App.color(
                        getLevels("myScheduleIcon"),
                        default.label.senary
                    )

                class Background(parent: MyScheduleCard) : PressableColor(parent) {
                    override val level: String = "background"
                    override val normalDefault: Color get() = default.accent.primary
                    override val pressedDefault: Color get() = default.accent.primary
                }
            }

            class Card(parent: Timeline) : UIColor(parent) {
                override val level: String = "card"

                val background = Background(this)
                val title get() = App.color(getLevels("title"), default.label.primary)
                val subtitle get() = App.color(getLevels("subtitle"), default.label.secondary)
                val border get() = App.color(getLevels("border"), default.fill.tertiary)
                val myScheduleIcon
                    get() = App.color(
                        getLevels("myScheduleIcon"),
                        default.accent.primary
                    )

                class Background(parent: Card) : PressableColor(parent) {
                    override val level: String = "background"
                    override val normalDefault: Color get() = default.background.secondary
                    override val pressedDefault: Color get() = default.fill.tertiary
                }
            }

            class HourLine(parent: Timeline) : UIColor(parent) {
                override val level: String = "timeIndicator"

                val label get() = App.color(getLevels("label"), default.label.primary)
                val background get() = App.color(getLevels("line"), default.background.primary)
            }
        }
    }

    val activityDetail = ActivityDetail(this)

    class ActivityDetail(parent: EventColor) : ScreenColor(parent) {
        override val level: String = "activityDetail"
        val header = Header(this)

        class Header(parent: ActivityDetail) : UIColor(parent) {
            override val level: String = "header"
            val myActivityIcon
                get() = App.color(
                    getLevels("myActivityIcon"),
                    default.accent.primary
                )
            val title get() = App.color(getLevels("title"), default.label.primary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
        }

        val mainSchedule = MainSchedule(this)

        class MainSchedule(parent: ActivityDetail) : UIColor(parent) {
            override val level: String = "mainSchedule"

            val stage = Stage(this)

            class Stage(parent: MainSchedule) : UIColor(parent) {
                override val level: String = "stage"
                val icon get() = App.color(getLevels("icon"), default.label.quaternary)
                val name get() = App.color(getLevels("name"), default.label.quaternary)
                val mapPin get() = App.color(getLevels("mapPin"), default.accent.primary)
            }

            val date = Date(this)

            class Date(parent: MainSchedule) : UIColor(parent) {
                override val level: String = "stage"
                val icon get() = App.color(getLevels("icon"), default.label.quaternary)
                val day get() = App.color(getLevels("day"), default.label.quaternary)
                val hours get() = App.color(getLevels("hours"), default.label.tertiary)
            }

            val mySchedule = MySchedule(this)

            class MySchedule(parent: MainSchedule) : SelectableColor(parent) {
                override val level: String = "mySchedule"
                override val selectedDefault: Color get() = default.accent.primary
                override val normalDefault: Color get() = default.accent.primary
            }
        }

        val upcomingTimes = UpcomingTimes(this)

        class UpcomingTimes(parent: ActivityDetail) : UIColor(parent) {
            override val level: String = "upcomingTimes"
            val title get() = App.color(getLevels("title"), default.label.secondary)
            val card = Card(this)

            class Card(parent: UpcomingTimes) : UIColor(parent) {
                override val level: String = "card"

                val background = Background(this)

                class Background(parent: Card) : PressableColor(parent) {
                    override val level: String = "background"
                    override val normalDefault: Color get() = default.background.secondary
                    override val pressedDefault: Color get() = default.fill.tertiary
                }

                val border get() = App.color(getLevels("border"), default.fill.tertiary)
                val shadow get() = App.color(getLevels("shadow"), default.fill.secondary)
            }

            val schedule = Schedule(this)

            class Schedule(parent: UpcomingTimes) : UIColor(parent) {
                override val level: String = "mainSchedule"

                val title get() = App.color(getLevels("title"), default.label.primary)
                val stage = Stage(this)

                class Stage(parent: Schedule) : UIColor(parent) {
                    override val level: String = "stage"
                    val icon get() = App.color(getLevels("icon"), default.label.quaternary)
                    val name get() = App.color(getLevels("name"), default.label.quaternary)
                    val mapPin get() = App.color(getLevels("mapPin"), default.accent.primary)
                }

                val date = Date(this)

                class Date(parent: Schedule) : UIColor(parent) {
                    override val level: String = "stage"
                    val icon get() = App.color(getLevels("icon"), default.label.quaternary)
                    val day get() = App.color(getLevels("day"), default.label.quaternary)
                    val hours get() = App.color(getLevels("hours"), default.label.tertiary)
                }

                val mySchedule = MySchedule(this)

                class MySchedule(parent: Schedule) : SelectableColor(parent) {
                    override val level: String = "mySchedule"
                    override val normalDefault: Color get() = default.accent.primary
                    override val selectedDefault: Color get() = default.accent.primary
                }
            }
        }

        val tags = TagColor(this)

        val description = Description(this)

        class Description(parent: ActivityDetail) : UIColor(parent) {
            override val level: String = "description"
            val title get() = App.color(getLevels("title"), default.label.secondary)
            val text get() = App.color(getLevels("text"), default.label.quinary)
            val showMore get() = App.color(getLevels("showMore"), default.accent.primary)
        }
    }

    val performersList = PerformersList(this)

    class PerformersList(parent: EventColor) : ScreenColor(parent) {
        override val level: String = "performersList"
        val separator get() = App.color(getLevels("separator"), default.fill.primary)

        val cell = Cell(this)
        val filters = FilteringBarColor(this)
        val empty = Empty(this)

        class Empty(parent: PerformersList) : UIColor(parent), IEmptyViewColors {
            override val level: String = "empty"
            override val title get() = App.color(getLevels("title"), default.label.primary)
            override val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
        }

        class Cell(parent: PerformersList) : UIColor(parent) {
            override val level: String = "cell"
            val name get() = App.color(getLevels("name"), default.label.secondary)
            val subtitle get() = App.color(getLevels("subtitle"), default.label.tertiary)
            val myPerformerIcon get() = App.color(getLevels("myPerformerIcon"), default.accent.primary)

            val background = Background(this)

            class Background(parent: Cell) : PressableColor(parent) {
                override val level: String = "background"
                override val normalDefault: Color get() = default.background.primary
                override val pressedDefault: Color get() = default.fill.tertiary
            }
        }
    }
}
