package com.greencopper.core.draftcontent

import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockDraftContentManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class IsDraftContentEnabledConditionTest : CoroutineTest() {

    private val manager = MockDraftContentManager()
    private val condition = IsDraftContentEnabledCondition(manager)

    override fun afterEach() {}

    @Test
    fun givenNoPasscode_check_returnsFalse() {
        manager.passcodeReturnValue = { null }
        assertThat(condition.check()).isFalse
    }

    @Test
    fun givenNoPasscode_checkFlow_returnsFalse() = runTest {
        manager.passcodeReturnValue = { null }
        assertThat(condition.checkFlow().first()).isFalse
    }

     @Test
    fun givenPasscode_check_returnsTrue() {
        manager.passcodeReturnValue = { "testpasscode" }
        assertThat(condition.check()).isTrue
    }

    @Test
    fun givenPasscode_checkFlow_returnsTrue() = runTest {
        manager.passcodeReturnValue = { "testpasscode" }
        assertThat(condition.checkFlow().first()).isTrue
    }
}
