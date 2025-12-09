package com.greencopper.interfacekit.onboarding

import androidx.fragment.app.FragmentManager
import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionSet
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfiguration
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigurationHolder
import com.greencopper.interfacekit.present
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteAppOnboardingManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val onboardingConfigHolder = OnboardingConfigurationHolder()
    private val conditionChecker = MockConditionChecker()
    private val mockFragmentManager = mockk<FragmentManager>()

    private val appOnboardingManager = ConcreteAppOnboardingManager(
        onboardingConfigHolder,
        conditionChecker,
    )

    private var presentCalled = false

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        RootLayoutHolder().setRootLayout(mockk())

        mockkStatic("com.greencopper.interfacekit.FragmentManagerExtensionsKt")
        every { any<FragmentManager>().present(any(), any(), any<String>()) } answers { presentCalled = true }
        every { mockFragmentManager.fragments } returns emptyList()
        every { mockFragmentManager.findFragmentByTag(any()) } returns null
    }

    override fun afterEach() {}

    @Test
    fun givenConfigAndPages_subscribeAppOnboarding_presentsOnboarding() {
        mockBundleConstructor()
        onboardingConfigHolder.currentConfiguration.value = OnboardingConfiguration(listOf(
            OnboardingPageInfo("", OnboardingPageKey("", 1))
        ))
        appOnboardingManager.checkAppOnboarding(mockFragmentManager, 0)

        assertThat(presentCalled).isTrue
    }

    @Test
    fun givenNoConfig_subscribeAppOnboarding_doesNotPresentOnboarding() {
        appOnboardingManager.checkAppOnboarding(mockFragmentManager, 0)

        assertThat(presentCalled).isFalse
    }

    @Test
    fun givenConfigWithNoPages_subscribeAppOnboarding_doesNotPresentOnboarding() {
        onboardingConfigHolder.currentConfiguration.value = OnboardingConfiguration(emptyList())
        appOnboardingManager.checkAppOnboarding(mockFragmentManager, 0)

        assertThat(presentCalled).isFalse
    }

    @Test
    fun givenConfigWithNoAuthorizedPages_subscribeAppOnboarding_doesNotPresentOnboarding() {
        conditionChecker.mockCheckConditionSet = { false }
        onboardingConfigHolder.currentConfiguration.value = OnboardingConfiguration(listOf(
            OnboardingPageInfo("", OnboardingPageKey("", 1), null,
                ConditionSet("", mapOf("" to ConditionInfo(ConditionInfo.Key("", 1), null, false))))
        ))
        appOnboardingManager.checkAppOnboarding(mockFragmentManager, 0)

        assertThat(presentCalled).isFalse
    }
}
