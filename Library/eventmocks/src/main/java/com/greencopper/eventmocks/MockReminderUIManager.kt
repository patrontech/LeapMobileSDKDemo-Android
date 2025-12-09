package com.greencopper.eventmocks

import com.greencopper.event.reminders.ui.ReminderUIManager
import com.greencopper.interfacekit.navigation.layout.Layout

public class MockReminderUIManager : ReminderUIManager {

    public var onAddToMyScheduleCalled: Boolean = false
    override fun onAddToMySchedule(origin: Layout?) {
        onAddToMyScheduleCalled = true
    }

    public var showReminderUICalled: Layout? = null
    override fun showReminderUI(origin: Layout?) {
        showReminderUICalled = origin
    }
}
