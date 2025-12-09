package com.greencopper.testmocks.core

import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.coroutines.flow.Flow

public class MockDraftContentManager(
    public var passcodeReturnValue: () -> String? = { unimplemented() },
    public var passcodeFlowReturnValue: () -> Flow<String?> = { unimplemented() },
    public var deletePasscodeResult: () -> Unit = { unimplemented() },
    public var setPasscodeResult: (String) -> Unit = { unimplemented() },
) : DraftContentManager {

    override val passcode: String?
        get() = passcodeReturnValue()

    override val passcodeFlow: Flow<String?>
        get() = passcodeFlowReturnValue()

    override fun deletePasscode(): Unit = deletePasscodeResult()

    override suspend fun setPasscode(passcode: String): Unit = setPasscodeResult(passcode)
}
