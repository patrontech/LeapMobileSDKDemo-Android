package com.greencopper.interfacekit.onboarding.pages

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.testmocks.testSerializable
import org.junit.jupiter.api.Test

internal class OnboardingPageActionTest {

    @Test
    fun testSerializable_locationPermission() = testSerializable(
        OnboardingPageAction.LocationPermission("request"),
        OnboardingPageAction.LocationPermission.serializer(),
        OnboardingPageAction.LocationPermission.serializer()
    )

    @Test
    fun testSerializable_bluetoothPermission() = testSerializable(
        OnboardingPageAction.BluetoothPermission("request"),
        OnboardingPageAction.BluetoothPermission.serializer(),
        OnboardingPageAction.BluetoothPermission.serializer()
    )

    @Test
    fun testSerializable_notificationPermission() = testSerializable(
        OnboardingPageAction.NotificationPermission("test"),
        OnboardingPageAction.NotificationPermission.serializer(),
        OnboardingPageAction.NotificationPermission.serializer()
    )

    @Test
    fun testSerializable_present() = testSerializable(
        OnboardingPageAction.Present(
            FeatureInfo(FeatureKey("name", 1)),
            ConditionSet("predicate", mapOf())
        ),
        OnboardingPageAction.Present.serializer(),
        OnboardingPageAction.Present.serializer()
    )

    @Test
    fun testSerializable_complete() = testSerializable(
        OnboardingPageAction.Complete(true),
        OnboardingPageAction.Complete.serializer(),
        OnboardingPageAction.Complete.serializer()
    )

    @Test
    fun testSerializable_execute() = testSerializable(
        OnboardingPageAction.Execute(CommandInfo(CommandInfo.Key("name", 1))),
        OnboardingPageAction.Execute.serializer(),
        OnboardingPageAction.Execute.serializer()
    )
}
