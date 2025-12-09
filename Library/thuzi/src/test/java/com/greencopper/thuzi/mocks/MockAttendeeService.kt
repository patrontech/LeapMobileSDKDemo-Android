package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.services.attendee.AttendeeService

internal class MockAttendeeService : AttendeeService {

    var fetchAndDispatchCalled = false

    override fun fetchAndDispatch() {
        fetchAndDispatchCalled = true
    }
}