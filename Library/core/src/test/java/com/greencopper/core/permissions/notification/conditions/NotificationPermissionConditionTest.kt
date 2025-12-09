package com.greencopper.core.permissions.notification.conditions

import com.greencopper.core.permissions.AuthorizationStatus
import com.greencopper.testmocks.core.MockNotificationPermissionService
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class NotificationPermissionConditionTest {
    init {
        Toolkit.setupTest()
    }

    private val testService = MockNotificationPermissionService()
    private val condition = NotificationPermissionCondition(testService)

    @Nested
    @DisplayName("Authorized")
    inner class Authorized {
        @Test
        @DisplayName("When checkWith is called and authorizationStatus is AuthorizedAlways, Then it should return true")
        internal fun testAuthorizedAlwaysTrue() {
            testService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
            val result =
                condition.checkWith(NotificationPermissionCondition.NotificationPermissionConditionData("authorized"))
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When checkWith is called and authorizationStatus isn't AuthorizedAlways, Then it should return false")
        internal fun testAuthorizedAlwaysFalse() {
            testService.authorizationStatusMock = AuthorizationStatus.Denied
            val result =
                condition.checkWith(NotificationPermissionCondition.NotificationPermissionConditionData("authorized"))
            assertThat(result).isFalse
        }
    }

    @Nested
    @DisplayName("Denied")
    inner class Denied {
        @Test
        @DisplayName("When checkWith is called and authorizationStatus is Denied, Then it should return true")
        internal fun testAuthorizedAlwaysTrue() {
            testService.authorizationStatusMock = AuthorizationStatus.Denied
            val result =
                condition.checkWith(NotificationPermissionCondition.NotificationPermissionConditionData("denied"))
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When checkWith is called and authorizationStatus isn't Denied, Then it should return false")
        internal fun testAuthorizedAlwaysFalse() {
            testService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
            val result =
                condition.checkWith(NotificationPermissionCondition.NotificationPermissionConditionData("denied"))
            assertThat(result).isFalse
        }
    }

    @Nested
    @DisplayName("NotDetermined")
    inner class NotDetermined {
        @Test
        @DisplayName("When checkWith is called and authorizationStatus is NotDetermined, Then it should return true")
        internal fun testAuthorizedAlwaysTrue() {
            testService.authorizationStatusMock = AuthorizationStatus.NotDetermined
            val result =
                condition.checkWith(NotificationPermissionCondition.NotificationPermissionConditionData("notDetermined"))
            assertThat(result).isTrue
        }

        @Test
        @DisplayName("When checkWith is called and authorizationStatus isn't NotDetermined, Then it should return false")
        internal fun testAuthorizedAlwaysFalse() {
            testService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
            val result =
                condition.checkWith(NotificationPermissionCondition.NotificationPermissionConditionData("notDetermined"))
            assertThat(result).isFalse
        }
    }

    @Test
    @DisplayName("Check Flow")
    internal fun testAuthorizedAlwaysTrueFlow() {
        testService.authorizationStatusMock = AuthorizationStatus.AuthorizedAlways
        runTest {
            val result = condition.checkWithFlow(
                NotificationPermissionCondition.NotificationPermissionConditionData("authorized")
            ).first()
            assertThat(result).isTrue
        }
    }

    @Test
    @DisplayName("Check NotificationPermissionCondition.NotificationPermissionConditionData serializable")
    internal fun testSerializable() = testKiboSerializable(
        NotificationPermissionCondition.NotificationPermissionConditionData(
            "test"
        )
    )

    @Test
    @DisplayName("Check NotificationPermissionCondition.NotificationPermissionConditionData kibo serializable")
    internal fun testKiboSerializable() {
        testKiboSerializable(
            NotificationPermissionCondition.NotificationPermissionConditionData(
                "test"
            )
        )
    }

    @Test
    @DisplayName("Test data deserialize")
    internal fun deserializeTest() {
        val testData = NotificationPermissionCondition.NotificationPermissionConditionData("authorized")
        assertThat(condition.deserialize(testData.encodeToJsonElement()))
            .isEqualTo(testData)
    }
}
