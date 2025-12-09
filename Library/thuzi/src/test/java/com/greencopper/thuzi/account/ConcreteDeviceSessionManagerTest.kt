package com.greencopper.thuzi.account

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockCurrentProjectTagProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteDeviceSessionManagerTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val projectTagProvider = MockCurrentProjectTagProvider()

    private val deviceSessionManager = ConcreteDeviceSessionManager(
        localStorage = localStorage,
        singleThreadScope = testScope,
        currentProjectTagProvider = projectTagProvider,
    )

    override fun afterEach() {}

    @Test
    fun newDeviceSessionForEveryProject_remainsConsistentForEveryProject() {
        val session1 = deviceSessionManager.getDeviceSession("1")
        val session2 = deviceSessionManager.getDeviceSession("2")
        val session3 = deviceSessionManager.getDeviceSession("3")

        assertThat(session1).isNotEqualTo(session2)
        assertThat(session1).isNotEqualTo(session3)
        assertThat(session2).isNotEqualTo(session3)

        val session1_2 = deviceSessionManager.getDeviceSession("1")
        val session2_2 = deviceSessionManager.getDeviceSession("2")
        val session3_2 = deviceSessionManager.getDeviceSession("3")

        assertThat(session1).isEqualTo(session1_2)
        assertThat(session2).isEqualTo(session2_2)
        assertThat(session3).isEqualTo(session3_2)
    }

    @Test
    fun deviceSessionUpdatesAfterProjectChange_remainsConsistentDuringProjectChanges() {
        testScope.launch {
            val projectFlow = MutableSharedFlow<String>()
            projectTagProvider.currentProjectFlowImpl = { projectFlow }

            projectFlow.emit("1")
            projectFlow.emit("2")
            val session1 = deviceSessionManager.getDeviceSession("1")
            val session2 = deviceSessionManager.getDeviceSession("2")

            projectFlow.emit("1")
            projectFlow.emit("2")
            val session1_2 = deviceSessionManager.getDeviceSession("1")
            val session2_2 = deviceSessionManager.getDeviceSession("2")

            assertThat(session1).isNotEqualTo(session2)
            assertThat(session1).isEqualTo(session1_2)
            assertThat(session2).isEqualTo(session2_2)
        }
    }

    @Test
    fun logoutChangesDeviceSession() {
        val session1 = deviceSessionManager.getDeviceSession("1")
        deviceSessionManager.logout("1")
        val session1_2 = deviceSessionManager.getDeviceSession("1")

        assertThat(session1).isNotEqualTo(session1_2)
    }
}
