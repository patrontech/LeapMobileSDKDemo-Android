package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.account.DeviceSessionManager
import com.greencopper.thuzi.models.DeviceSession
import com.greencopper.toolkit.testing.unimplemented

public class MockDeviceSessionManager(
    var getDeviceSessionImpl: (String) -> DeviceSession = { unimplemented() },
    var logoutImpl: (String) -> Unit = { unimplemented() },
) : DeviceSessionManager {

    override fun getDeviceSession(project: String): DeviceSession = getDeviceSessionImpl(project)

    override fun logout(project: String) {
        logoutImpl(project)
    }
}
