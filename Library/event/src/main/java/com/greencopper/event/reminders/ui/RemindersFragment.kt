package com.greencopper.event.reminders.ui

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.core.services.track
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.RemindersFragmentBinding
import com.greencopper.event.metrics.scheduleReminders
import com.greencopper.event.reminders.RemindersLayoutData
import com.greencopper.event.reminders.viewmodel.RemindersViewModel
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.fragment.*
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.launch

internal class RemindersFragment : ParameterizedFragment<RemindersLayoutData>, RedirectableLayout, BottomSheetChild {

    constructor(remindersData: RemindersLayoutData) : super(remindersData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val binding: RemindersFragmentBinding by viewBinding(RemindersFragmentBinding::inflate)

    private val viewModel: RemindersViewModel by viewModel()
    private val localizationService: LocalizationService by App.lazy()

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val backgroundColor: Int
        get() = EventColor.schedule.reminders.background

    override val navigationBarColor: Int
        get() = backgroundColor

    override val screenColor: ScreenColor
        get() = EventColor.schedule

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (parentFragment is BottomSheetDialogFragmentContainer) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, insets.bottom)
                WindowInsetsCompat.CONSUMED
            }
        }

        setup()
        initOptions()
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.scheduleReminders(data.analytics.screenName)))
    }

    override fun restoreData(encodedData: String): RemindersLayoutData = KiboSerializable.decodeFromString(encodedData)

    private fun setup() {
        val textStyles = EventTextStyle.scheduleReminders
        val colors = EventColor.schedule.reminders

        binding.root.setBackgroundColor(colors.background)

        with(binding.remindersTitle) {
            setTextColor(colors.title)
            setFont(textStyles.title)
            text = localizationService.getString("event.reminders.title")
        }

        with(binding.remindersSubtitle) {
            setTextColor(colors.subtitle)
            setFont(textStyles.subtitle)
            text = localizationService.getString("event.reminders.subtitle")
        }
    }

    private fun initOptions() {
        binding.remindersRadioGroup.removeAllViews()

        val defaultInterval = viewModel.getDefaultInterval(requireContext())
        viewModel.setRemindersInterval(defaultInterval)

        viewModel.getIntervals()
            .map {
                ReminderIntervalViewData(it.value, localizationService.getString(it.label))
            }
            .forEach { interval ->
                val option = ReminderOptionView(requireContext())
                option.setup(
                    interval,
                )
                binding.remindersRadioGroup.addView(option)
                if (interval.minutes == defaultInterval) {
                    binding.remindersRadioGroup.check(option.id)
                }
            }

        binding.remindersRadioGroup.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    private val onCheckedChangeListener: OnCheckedChangeListener =
        OnCheckedChangeListener { group, checkedId ->
            val option = group.findViewById<ReminderOptionView>(checkedId)
            onReminderIntervalSelected(option.interval.minutes)
        }

    private fun onReminderIntervalSelected(interval: Int) = viewLifecycleOwner.lifecycleScope.launch {
        if (interval < 0) {
            viewModel.removeReminders()
        } else if (
            viewModel.areNotificationsEnabled(requireContext())
            && viewModel.getNotificationsAuthorizationStatus() == AuthorizationStatus.AuthorizedAlways
        ) {
            viewModel.setRemindersInterval(interval)
        } else if (
            viewModel.requestNotificationPermission(requireActivity())
        ) {
            viewModel.setRemindersInterval(interval)
        } else {
            with(binding.remindersRadioGroup) {
                children.firstOrNull { (it as? ReminderOptionView)?.interval?.minutes == -1 }?.let {
                    setOnCheckedChangeListener(null)
                    check(it.id)
                    setOnCheckedChangeListener(onCheckedChangeListener)
                }
            }
            return@launch
        }

        (parentFragment as? BottomSheetDialogFragmentContainer)?.dismiss()
    }

}
