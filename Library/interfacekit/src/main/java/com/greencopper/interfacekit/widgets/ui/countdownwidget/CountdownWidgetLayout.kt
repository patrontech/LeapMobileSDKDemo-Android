package com.greencopper.interfacekit.widgets.ui.countdownwidget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.parseRGBA
import com.greencopper.interfacekit.databinding.CountdownWidgetBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.pxToDp
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.setOtaHtmlTextOrGone
import com.greencopper.interfacekit.widgets.initializer.CountdownWidgetParameters
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.extensions.decodeHtmlString
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import kotlin.math.max

internal class CountdownWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<CountdownWidgetParameters>(context, attrs, defStyleAttr) {
    override fun getWidgetItemName(params: CountdownWidgetParameters): String = widgetCategory
    override val widgetCategory: String = "countdown_widget"

    override val binding: CountdownWidgetBinding =
        CountdownWidgetBinding.inflate(LayoutInflater.from(context), this)
    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val localizationService: LocalizationService by App.lazy()
    private val timezoneProvider: TimezoneProvider by App.lazy()
    private val styles = InterfaceKitTextStyle.countdownWidget

    init {
        with(binding) {
            cardView.strokeWidth = 1.dpToPx()
            cardView.strokeColor = InterfaceKitColor.countdownWidget.border

            title.setFont(styles.title)
            subtitle.setFont(styles.subtitle)
            endDate.setFont(styles.endDate)
            daysIndicator.setFont(styles.indicator)
            days.setFont(styles.indicatorLabel)
            hoursIndicator.setFont(styles.indicator)
            hours.setFont(styles.indicatorLabel)
            minutesIndicator.setFont(styles.indicator)
            minutes.setFont(styles.indicatorLabel)
            secondsIndicator.setFont(styles.indicator)
            seconds.setFont(styles.indicatorLabel)
            colon1.setFont(styles.indicatorLabel)
            colon2.setFont(styles.indicatorLabel)
            colon3.setFont(styles.indicatorLabel)

            days.text = localizationService.getString("interfaceKit.widget.countdown.days")
            hours.text = localizationService.getString("interfaceKit.widget.countdown.hours")
            minutes.text = localizationService.getString("interfaceKit.widget.countdown.minutes")
            seconds.text = localizationService.getString("interfaceKit.widget.countdown.seconds")
        }
    }

    override fun bind(params: CountdownWidgetParameters, screenName: String, origin: Layout, jobs: MutableList<Job>) {
        with(binding) {
            params.backgroundImage?.let { image ->
                jobs.add(
                    imageView.setImageFrom(image, origin.lifecycleScope)
                )
                params.digitBackgroundColor?.parseRGBA()?.let { color ->
                    listOf(daysCard, hoursCard, minutesCard, secondsCard).forEach { card ->
                        card.setCardBackgroundColor(color)
                    }
                }
                listOf(title, subtitle, endDate).forEach {
                    it.setShadowLayer(
                        /* radius = */ 2.dpToPx().toFloat(),
                        /* dx = */ 1.dpToPx().toFloat(),
                        /* dy = */ 1.dpToPx().toFloat(),
                        /* color = */ Color.BLACK
                    )
                }
                divider.elevation = 4.pxToDp()
                cardView.setCardBackgroundColor(Color.TRANSPARENT)
                cardView.elevation = 0f
            }

            params.backgroundColor?.parseRGBA()?.let {
                cardView.setCardBackgroundColor(it)
                cardView.elevation = 4.pxToDp()
            }

            subtitle.setOtaHtmlTextOrGone(localizationService, params.afterCountdown?.subtitle)

            val textColor = params.textColor.parseRGBA()
            listOf(
                title, subtitle, endDate, daysIndicator, days, hoursIndicator, hours, minutesIndicator, minutes,
                secondsIndicator, seconds, colon1, colon2, colon3
            )
                .forEach { textField ->
                    textField.setTextColor(textColor)
                }

            divider.backgroundTintList = ColorStateList.valueOf(textColor)

            var timeRemaining = getTimeRemaining(params.endDateTime)
            if (!timeRemaining.isOver()) {
                jobs.add(
                    origin.viewLifecycleOwner.lifecycleScope.launch {
                        while (!timeRemaining.isOver()) {
                            updateUI(params, timeRemaining)
                            delay(1000L)
                            timeRemaining = getTimeRemaining(params.endDateTime)
                        }
                        updateUI(params, timeRemaining)
                    })
            }

            updateUI(params, timeRemaining)
        }
    }

    private fun updateUI(params: CountdownWidgetParameters, timeRemaining: TimeRemaining) {
        with(binding) {
            val titleKey =
                if (timeRemaining.isOver() && params.afterCountdown != null) params.afterCountdown.title else params.title
            title.setOtaHtmlTextOrGone(localizationService, titleKey)

            endDate.text = formatEndDate(params.endDateTime, timeRemaining.isOver())
            endDate.isVisible = params.showDate

            daysIndicator.text = timeRemaining.days.formatWithTwoDigits()
            hoursIndicator.text = timeRemaining.hours.formatWithTwoDigits()
            minutesIndicator.text = timeRemaining.minutes.formatWithTwoDigits()
            secondsIndicator.text = timeRemaining.seconds.formatWithTwoDigits()

            subtitle.isVisible = timeRemaining.isOver()
            countdownContainer.isVisible = !timeRemaining.isOver() || params.afterCountdown == null

            val sb = StringBuilder()
            sb.append(localizationService.getString(titleKey)?.decodeHtmlString().toString().lowercase() + ", ")
            if (timeRemaining.isOver() && params.afterCountdown?.subtitle != null) {
                sb.append(localizationService.getString(params.afterCountdown.subtitle).decodeHtmlString().toString() + ", ")
            } else {
                sb.append("${timeRemaining.days} ${localizationService.getString("interfaceKit.widget.countdown.days")} ")
                sb.append("${timeRemaining.hours} ${localizationService.getString("interfaceKit.widget.countdown.hours")} ")
                sb.append("${timeRemaining.minutes} ${localizationService.getString("interfaceKit.widget.countdown.minutes")} ")
                sb.append("${timeRemaining.seconds} ${localizationService.getString("interfaceKit.widget.countdown.seconds")}, ")
            }
            if (params.showDate) sb.append(formatEndDate(params.endDateTime, timeRemaining.isOver()))

            cardView.contentDescription = sb.toString()
        }
    }

    private fun formatEndDate(date: ZonedDateTime, isDone: Boolean): String {
        val labelKey = if (isDone) "interfaceKit.widget.countdown.ended_on" else "interfaceKit.widget.countdown.ends_on"
        val label = localizationService.getString(labelKey)

        var pattern = DateFormat.getBestDateTimePattern(App.locale, "MMM d, h:mm")
        if (DateFormat.is24HourFormat(context)) pattern = pattern.replace("a", "").replace("h", "H")

        return label + DateTimeFormatter.ofPattern(pattern)
            .withChronology(IsoChronology.INSTANCE).format(date.withZoneSameInstant(timezoneProvider.zoneId))
    }

    private fun getTimeRemaining(targetDateTime: ZonedDateTime): TimeRemaining {
        val now = ZonedDateTime.now()
        val duration = Duration.between(now, targetDateTime.withZoneSameInstant(now.zone))

        val days = max(0L, duration.toDays())
        val hours = max(0L, duration.toHours() % 24)
        val minutes = max(0L, duration.toMinutes() % 60)
        val seconds = max(0L, duration.seconds % 60)

        return TimeRemaining(days, hours, minutes, seconds)
    }

    data class TimeRemaining(val days: Long, val hours: Long, val minutes: Long, val seconds: Long) {
        fun isOver() = days <= 0L && hours <= 0L && minutes <= 0L && seconds <= 0L
    }

    private fun Long.formatWithTwoDigits(): String = String.format("%02d", this)
}
