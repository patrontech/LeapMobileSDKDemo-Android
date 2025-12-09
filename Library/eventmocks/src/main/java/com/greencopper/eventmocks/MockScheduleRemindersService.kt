package com.greencopper.eventmocks

import com.greencopper.event.reminders.ScheduleRemindersService

public class MockScheduleRemindersService : ScheduleRemindersService {
    override fun collectScheduleReminders() {}

    public var currentReminderInterval: Int? = null
    override fun setReminderInterval(interval: Int) {
        currentReminderInterval = interval
    }
}
