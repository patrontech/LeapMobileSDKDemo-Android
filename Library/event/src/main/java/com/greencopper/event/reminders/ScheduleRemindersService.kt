package com.greencopper.event.reminders

public interface ScheduleRemindersService {
    public fun collectScheduleReminders()
    public fun setReminderInterval(interval: Int)
}