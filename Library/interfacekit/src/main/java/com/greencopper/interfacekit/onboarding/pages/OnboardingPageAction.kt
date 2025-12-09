package com.greencopper.interfacekit.onboarding.pages

import androidx.annotation.StringDef
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import kotlinx.serialization.Serializable

@Serializable
public sealed class OnboardingPageAction {

    @Serializable
    public data class LocationPermission(
        @LocationRequest val request: String,
        val analyticsEvent: String? = null
    ) :
        OnboardingPageAction() {
        @Retention(AnnotationRetention.SOURCE)
        @StringDef(WHEN_IN_USE, ALWAYS)
        public annotation class LocationRequest

        public companion object {
            public const val WHEN_IN_USE: String = "whenInUse"
            public const val ALWAYS: String = "always"
            public const val key: String = "locationPermissions"
        }
    }

    @Serializable
    public data class BluetoothPermission(
        val request: String,
        val analyticsEvent: String? = null
    ) : OnboardingPageAction() {
        public companion object {
            public const val key: String = "bluetoothPermissions"
        }
    }

    @Serializable
    public data class NotificationPermission(
        val analyticsEvent: String? = null
    ) : OnboardingPageAction() {
        public companion object {
            public const val key: String = "notificationPermissions"
        }
    }

    @Serializable
    public data class Present(
        val featureInfo: FeatureInfo,
        val completionConditions: ConditionSet,
        val analyticsEvent: String? = null
    ) : OnboardingPageAction() {
        public companion object {
            public const val key: String = "present"
        }
    }

    @Serializable
    public data class Complete(
        val persistAsCompleted: Boolean,
        val analyticsEvent: String? = null
    ) : OnboardingPageAction() {
        public companion object {
            public const val key: String = "complete"
        }
    }

    @Serializable
    public data class Execute(
        val commandInfo: CommandInfo
    ) : OnboardingPageAction() {
        public companion object {
            public const val key: String = "execute"
        }
    }
}
