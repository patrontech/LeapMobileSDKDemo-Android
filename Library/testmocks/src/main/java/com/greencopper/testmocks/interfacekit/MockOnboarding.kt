package com.greencopper.testmocks.interfacekit

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

public data class MockOnboardingData(val data: String) : KiboSerializable<MockOnboardingData> {
    override fun getSerializer(): KSerializer<MockOnboardingData> = serializer()
}

public class MockOnboardingPageLayout : ParameterizedFragment<MockOnboardingData>(null), OnboardingPageLayout {
    override val onboardingScreenViewEvent: ScreenViewEvent? = ScreenViewEvent(
        Screen("mock", "mock")
    )

    override val onboardingPageId: String = "pageId"

    override val screenColor: ScreenColor?
        get() = null

    override fun restoreData(encodedData: String): MockOnboardingData {
        return KiboSerializable.decodeFromString(encodedData)
    }
}

public class MockOnboardingPageInitializer(public var showInSequence: Boolean = true) : OnboardingPageInitializer {
    override fun resolve(params: JsonElement?, pageId: String): OnboardingPageLayout {
        return MockOnboardingPageLayout()
    }

    override fun showInSequence(): Boolean = showInSequence

    public companion object {
        public val key: OnboardingPageKey = OnboardingPageKey("Mock.MockOnboardingPage", 1)
    }
}
