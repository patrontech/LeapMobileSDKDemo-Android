package com.greencopper.interfacekit.onboarding.maincard

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageAction
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MainActionCardDataActionButtonTest {

    @Test
    @DisplayName("Given action is of present type, When toPageAction is called, Then Present is returned")
    fun toPageActionShouldReturnPresent() {
        val action = MainActionCardDataActionButton.Action(
            type = "present",
            request = null,
            feature = mockk(),
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThat(action.toPageAction()).isInstanceOf(OnboardingPageAction.Present::class.java)
    }

    @Test
    @DisplayName("Given action is of present type and feature info is null, When toPageAction is called, Then IllegalStateException should be raised")
    fun toPageActionPresentWithNullFeatureShouldThrow() {
        val action = MainActionCardDataActionButton.Action(
            type = "present",
            request = null,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThrows<IllegalStateException> {
            action.toPageAction()
        }
    }

    @Test
    @DisplayName("Given action is of present type and completion is null, When toPageAction is called, Then IllegalStateException should be raised")
    fun toPageActionPresentWithNullCompletionShouldThrow() {
        val action = MainActionCardDataActionButton.Action(
            type = "present",
            request = null,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThrows<IllegalStateException> {
            action.toPageAction()
        }
    }

    @Test
    @DisplayName("Given action is of execute type, When toPageAction is called, Then Execute is returned")
    fun toPageActionShouldReturnExecute() {
        val action = MainActionCardDataActionButton.Action(
            type = "execute",
            request = null,
            command = mockk(),
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThat(action.toPageAction()).isInstanceOf(OnboardingPageAction.Execute::class.java)
    }

    @Test
    @DisplayName("Given action is of execute type and command info is null, When toPageAction is called, Then IllegalStateException should be raised")
    fun toPageActionExecuteWithNullCommandShouldThrow() {
        val action = MainActionCardDataActionButton.Action(
            type = "execute",
            request = null,
            feature = null,
            command = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThrows<IllegalStateException> {
            action.toPageAction()
        }
    }

    @Test
    @DisplayName("Given action is of location type, When toPageAction is called, Then LocationPermission is returned")
    fun toPageActionShouldReturnLocationPermission() {
        val action = MainActionCardDataActionButton.Action(
            type = "locationPermissions",
            request = "whenInUse",
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThat(action.toPageAction()).isInstanceOf(OnboardingPageAction.LocationPermission::class.java)
    }

    @Test
    @DisplayName("Given action is of location type and request is null, When toPageAction is called, Then IllegalStateException should be raised")
    fun toPageActionLocationWithNullRequestShouldThrow() {
        val action = MainActionCardDataActionButton.Action(
            type = "locationPermissions",
            request = null,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThrows<IllegalStateException> {
            action.toPageAction()
        }
    }

    @Test
    @DisplayName("Given action is of bluetooth type, When toPageAction is called, Then BluetoothPermission is returned")
    fun toPageActionShouldReturnBluetoothPermission() {
        val action = MainActionCardDataActionButton.Action(
            type = "bluetoothPermissions",
            request = "whenInUse",
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThat(action.toPageAction()).isInstanceOf(OnboardingPageAction.BluetoothPermission::class.java)
    }

    @Test
    @DisplayName("Given action is of bluetooth type and request is null, When toPageAction is called, Then IllegalStateException should be raised")
    fun toPageActionBluetoothWithNullRequestShouldThrow() {
        val action = MainActionCardDataActionButton.Action(
            type = "bluetoothPermissions",
            request = null,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThrows<IllegalStateException> {
            action.toPageAction()
        }
    }

    @Test
    @DisplayName("Given action is of notification permission type, When toPageAction is called, Then NotificationPermission is returned")
    fun toPageActionNotificationPermissionShouldReturnNotificationPermission() {
        val action = MainActionCardDataActionButton.Action(
            type = OnboardingPageAction.NotificationPermission.key,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThat(action.toPageAction()).isInstanceOf(OnboardingPageAction.NotificationPermission::class.java)
    }

    @Test
    @DisplayName("Given action is of complete type, When toPageAction is called, Then Complete is returned")
    fun toPageActionShouldReturnComplete() {
        val action = MainActionCardDataActionButton.Action(
            type = "complete",
            request = null,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThat(action.toPageAction()).isInstanceOf(OnboardingPageAction.Complete::class.java)
    }

    @Test
    @DisplayName("Given action is of unknown type, When toPageAction is called, Then IllegalArgumentException is raised")
    fun toPageActionShouldThrow() {
        val action = MainActionCardDataActionButton.Action(
            type = "null",
            request = null,
            feature = null,
            completion = mockk(),
            persistAsCompleted = true
        )
        assertThrows<IllegalArgumentException> {
            action.toPageAction()
        }
    }

    @Test
    fun serialize() {
        Toolkit.setupTest()
        val action = MainActionCardDataActionButton.Action(
            type = "complete",
            request = null,
            feature = null,
            completion = ConditionSet("predicate", emptyMap()),
            persistAsCompleted = true
        )
        val button = MainActionCardDataActionButton("mainAction", action)
        val encoded = button.encodeToString()
        val decoded = KiboSerializable.decodeFromString<MainActionCardDataActionButton>(encoded)

        assertThat(decoded).isInstanceOf(MainActionCardDataActionButton::class.java)
    }
}