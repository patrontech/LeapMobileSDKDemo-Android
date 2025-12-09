package com.greencopper.interfacekit.onboarding.maincard

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageAction
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class MainActionCardDataActionButton(val title: String, val action: Action) :
    KiboSerializable<MainActionCardDataActionButton> {

    @Serializable
    public data class Action(
        val type: String,
        val request: String? = null,
        val feature: FeatureInfo? = null,
        val command: CommandInfo? = null,
        val completion: ConditionSet? = null,
        val persistAsCompleted: Boolean? = null,
        val analyticsEvent: String? = null
    )

    override fun getSerializer(): KSerializer<MainActionCardDataActionButton> = serializer()
}

public fun MainActionCardDataActionButton.Action.toPageAction(): OnboardingPageAction =
    when (type) {
        OnboardingPageAction.Present.key -> {
            completion
                ?: throw IllegalStateException("Action $this is Present but no condition set info is found")
            feature?.let { featureInfo ->
                OnboardingPageAction.Present(featureInfo, completion, analyticsEvent)
            }
                ?: throw IllegalStateException("Action $this is Present but no feature info is found")
        }

        OnboardingPageAction.Execute.key -> {
            command?.let { commandInfo ->
                OnboardingPageAction.Execute(commandInfo)
            } ?: throw IllegalStateException("Action $this is Execute but no command info is found")
        }

        OnboardingPageAction.Complete.key -> OnboardingPageAction.Complete(
            persistAsCompleted ?: true,
            analyticsEvent
        )
        
        OnboardingPageAction.LocationPermission.key -> OnboardingPageAction.LocationPermission(
            request
                ?: throw IllegalStateException("Action $this is LocationPermission but no request info is found"),
            analyticsEvent
        )
        OnboardingPageAction.BluetoothPermission.key -> OnboardingPageAction.BluetoothPermission(
            request
                ?: throw java.lang.IllegalStateException("Action $this is BluetoothPermission but no request info is found"),
            analyticsEvent
        )

        OnboardingPageAction.NotificationPermission.key -> OnboardingPageAction.NotificationPermission(
            analyticsEvent
        )
        else -> throw IllegalArgumentException("Action $this type is not handled.")
    }
