package com.greencopper.event.textstyle

import com.greencopper.interfacekit.empty.ui.EmptyViewTextStyles
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarTextStyle
import com.greencopper.interfacekit.lists.ui.EmptyViewTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.*

internal object EventTextStyle : UITextStyle() {

    override val level: String = "event"

    val scheduleReminders = ScheduleReminders(this)

    class ScheduleReminders(parent: EventTextStyle) : UITextStyle(parent) {
        override val level: String = "scheduleReminders"

        val title get() = toIKFont("title", IKFont.TextStyle.titleM)
        val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyL)
        val option get() = toIKFont("option", IKFont.TextStyle.bodyXL)
    }

    val schedule = Schedule(this)

    class Schedule(parent: EventTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "schedule"

        val header = Header(this)

        class Header(parent: Schedule) : UITextStyle(parent) {
            override val level: String = "header"

            val filters = FilteringBarTextStyle(this)

            val datePicker = DatePicker(this)

            class DatePicker(parent: Header) : UITextStyle(parent) {
                override val level: String = "datepicker"

                val selected = Selected(this)

                class Selected(parent: DatePicker) : UITextStyle(parent) {
                    override val level: String = "selected"

                    val topLine get() = toIKFont("topLine", IKFont.TextStyle.captionL)
                    val bottomLine get() = toIKFont("bottomLine", IKFont.TextStyle.titleS)
                }

                val normal = Normal(this)

                class Normal(parent: DatePicker) : UITextStyle(parent) {
                    override val level: String = "normal"

                    val topLine get() = toIKFont("topLine", IKFont.TextStyle.captionS)
                    val bottomLine get() = toIKFont("bottomLine", IKFont.TextStyle.titleS)
                }
            }

            val oneDay = OneDay(this)

            class OneDay(parent: Header) : UITextStyle(parent) {
                override val level: String = "oneDay"

                val label get() = toIKFont("label", IKFont.TextStyle.titleS)
            }
        }

        val list = List(this)

        class List(parent: Schedule) : UITextStyle(parent) {
            override val level: String = "list"

            val daySeparator = DaySeparator(this)

            class DaySeparator(parent: List) : UITextStyle(parent) {
                override val level: String = "daySeparator"

                val label get() = toIKFont("label", IKFont.TextStyle.titleS)
            }

            val timeSeparator = TimeSeparator(this)

            class TimeSeparator(parent: List) : UITextStyle(parent) {
                override val level: String = "timeSeparator"

                val label get() = toIKFont("label", IKFont.TextStyle.titleXS)
            }

            val card = Card(this)

            class Card(parent: List) : UITextStyle(parent) {
                override val level: String = "card"

                val name get() = toIKFont("name", IKFont.TextStyle.headlineM)
                val hours get() = toIKFont("hours", IKFont.TextStyle.bodyS)
                val stage get() = toIKFont("stage", IKFont.TextStyle.headlineS)
            }

            val nextButton = NextButton(this)

            class NextButton(parent: List) : UITextStyle(parent) {
                override val level: String = "nextButton"

                val label get() = toIKFont("name", IKFont.TextStyle.headlineM)
            }
        }

        val timeline = Timeline(this)

        class Timeline(parent: Schedule) : UITextStyle(parent) {
            override val level: String = "timeline"

            val hourLine = HourLine(this)

            class HourLine(parent: Timeline) : UITextStyle(parent) {
                override val level: String = "hourLine"

                val label get() = toIKFont("label", IKFont.TextStyle.headlineM)
            }

            val stage = Stage(this)

            class Stage(parent: Timeline) : UITextStyle(parent) {
                override val level: String = "stage"

                val title get() = toIKFont("title", IKFont.TextStyle.titleXS)
            }

            val myScheduleCard = MyScheduleCard(this)

            class MyScheduleCard(parent: Timeline) : UITextStyle(parent) {
                override val level: String = "myScheduleCard"

                val title get() = toIKFont("title", IKFont.TextStyle.headlineM)
                val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyS)
            }

            val card = Card(this)

            class Card(parent: Timeline) : UITextStyle(parent) {
                override val level: String = "card"

                val title get() = toIKFont("title", IKFont.TextStyle.headlineM)
                val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyS)
            }
        }

        val empty = EmptyViewTextStyles(this)
    }

    val activitiesList = ActivitiesList(this)

    class ActivitiesList(parent: EventTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "activitiesList"

        val filters = FilteringBarTextStyle(this)
        val cell = Cell(this)
        val empty = Empty(this)

        class Empty(parent: ActivitiesList) : UITextStyle(parent), EmptyViewTextStyle {
            override val level: String = "empty"

            override val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            override val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyM)
        }

        class Cell(parent: ActivitiesList) : UITextStyle(parent) {
            override val level: String = "cell"

            val name get() = toIKFont("name", IKFont.TextStyle.headlineM)
            val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.headlineS)
        }
    }

    val activityDetail = ActivityDetail(this)

    class ActivityDetail(parent: EventTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "activityDetail"

        val header = Header(this)

        class Header(parent: ActivityDetail) : UITextStyle(parent) {
            override val level: String = "header"

            val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.headlineS)
        }

        val mainSchedule = MainSchedule(this)

        class MainSchedule(parent: ActivityDetail) : UITextStyle(parent) {
            override val level: String = "mainSchedule"

            val day get() = toIKFont("day", IKFont.TextStyle.headlineM)
            val hours get() = toIKFont("hours", IKFont.TextStyle.bodyS)
            val stage get() = toIKFont("stage", IKFont.TextStyle.headlineM)
        }

        val upcomingTimes = UpcomingTimes(this)

        class UpcomingTimes(parent: ActivityDetail) : UITextStyle(parent) {
            override val level: String = "upcomingTimes"

            val title get() = toIKFont("title", IKFont.TextStyle.headlineL)

            val schedule = Schedule(this)

            class Schedule(parent: UpcomingTimes) : UITextStyle(parent) {
                override val level: String = "schedule"

                val title get() = toIKFont("title", IKFont.TextStyle.headlineL)
                val stage get() = toIKFont("stage", IKFont.TextStyle.headlineM)
                val day get() = toIKFont("day", IKFont.TextStyle.headlineM)
                val hours get() = toIKFont("hours", IKFont.TextStyle.bodyS)
            }
        }

        val description = Description(this)

        class Description(parent: ActivityDetail) : UITextStyle(parent) {
            override val level: String = "description"

            val title get() = toIKFont("title", IKFont.TextStyle.headlineL)
            val text get() = toIKFont("text", IKFont.TextStyle.bodyL)
            val showMore get() = toIKFont("showMore", IKFont.TextStyle.bodyM)
        }
    }

    val performersList = PerformersList(this)

    class PerformersList(parent: EventTextStyle) : ScreenTextStyle(parent) {
        override val level: String = "performersList"

        val filters = FilteringBarTextStyle(this)
        val cell = Cell(this)
        val empty = Empty(this)

        class Empty(parent: PerformersList) : UITextStyle(parent), EmptyViewTextStyle {
            override val level: String = "empty"

            override val title get() = toIKFont("title", IKFont.TextStyle.titleL)
            override val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.bodyM)
        }

        class Cell(parent: PerformersList) : UITextStyle(parent) {
            override val level: String = "cell"

            val name get() = toIKFont("name", IKFont.TextStyle.headlineM)
            val subtitle get() = toIKFont("subtitle", IKFont.TextStyle.headlineS)
        }
    }
}
