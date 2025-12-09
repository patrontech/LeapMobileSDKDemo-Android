package com.greencopper.interfacekit.rootview

import androidx.fragment.app.FragmentManager
import com.greencopper.interfacekit.navigation.feature.FeatureInitializer
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfiguration
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigurationHolder
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.core.MockCurrentProjectTagProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ConcreteRootLayoutManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val featureResolver: FeatureResolver = mockkClass(FeatureResolver::class)
    private val rootLayoutHolder: RootLayoutHolder = spyk(RootLayoutHolder())
    private val featureInfo = FeatureInfo(FeatureKey("name", 98), null)
    private val onboardingConfigHolder: OnboardingConfigurationHolder = OnboardingConfigurationHolder()
    private val rootViewConfiguration = RootViewConfiguration(featureInfo)
    private val rootViewConfigHolder = RootViewConfigurationHolder()
    private val conditionChecker = MockConditionChecker()
    private val currentProjectTagProvider = MockCurrentProjectTagProvider(
        currentProjectImpl = { "project" },
        currentProjectFlowImpl = { flowOf("project") },
    )

    override fun afterEach() {}

    @Test
    fun setupRootLayout_withOnboardingConfig_shouldResolveMatchingOnboarding() {
        mockkObject(OnboardingContainerLayout)
        every  { OnboardingContainerLayout.newInstance(any()) } returns mockk()

        val featureInitializer: FeatureInitializer = mockk()
        every { featureInitializer.redirectionHashFor(any()) } returns mockk()
        every { featureResolver.resolveInitializer(any()) } returns featureInitializer

        val onboardingConfiguration = OnboardingConfiguration(listOf(
            OnboardingPageInfo("id", OnboardingPageKey("name", 1))
        ))
        onboardingConfigHolder.currentConfiguration.value = onboardingConfiguration

        val concreteRootLayoutResolver = ConcreteRootLayoutManager(
            featureResolver,
            onboardingConfigHolder,
            rootLayoutHolder,
            rootViewConfigHolder,
            currentProjectTagProvider,
            conditionChecker,
            testScope
        )

        val mockFragmentManager: FragmentManager = mockk()
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } returns Unit

        runTest {
            concreteRootLayoutResolver.setupRootLayout(mockFragmentManager, false)
            rootViewConfigHolder.tryEmit(rootViewConfiguration)
            verify(exactly = 1, timeout = 1000) {
                featureResolver.resolveInitializer(featureInfo)
            }
        }
    }

    @Test
    fun setupRootLayout_withRootViewConfiguration_shouldReturnTheMatchingOne() {
        every {
            featureResolver.resolve(any())
        } returns mockkClass(Layout::class)
        val concreteRootLayoutResolver = ConcreteRootLayoutManager(
            featureResolver,
            onboardingConfigHolder,
            rootLayoutHolder,
            rootViewConfigHolder,
            currentProjectTagProvider,
            conditionChecker,
            testScope
        )
        val mockFragmentManager: FragmentManager = mockk()
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } returns Unit

        runTest {
            concreteRootLayoutResolver.setupRootLayout(mockFragmentManager, false)
            rootViewConfigHolder.tryEmit(rootViewConfiguration)
            verify(exactly = 1, timeout = 2000) {
                featureResolver.resolve(FeatureInfo(FeatureKey("name", 98), null))
            }
        }
    }

    @Test
    fun setupRootLayout_withoutAlreadySetup_shouldUpdateRootLayout() {
        every {
            featureResolver.resolve(any())
        } returns mockkClass(Layout::class)
        val concreteRootLayoutResolver = ConcreteRootLayoutManager(
            featureResolver,
            onboardingConfigHolder,
            rootLayoutHolder,
            rootViewConfigHolder,
            currentProjectTagProvider,
            conditionChecker,
            testScope
        )
        val mockFragmentManager: FragmentManager = mockk()
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } returns Unit

        runTest {
            concreteRootLayoutResolver.setupRootLayout(mockFragmentManager, false)
            rootViewConfigHolder.tryEmit(rootViewConfiguration)
            verify(exactly = 1, timeout = 2000) {
                rootLayoutHolder.setRootLayout(any())
            }
        }
    }

    @Test
    fun setupNullRootLayout_shouldDoNothing() {
        every {
            featureResolver.resolve(FeatureInfo(FeatureKey("name", 98), null))
        } answers { throw IllegalStateException("Mockk should not be reached") }
        val concreteRootLayoutResolver = ConcreteRootLayoutManager(
            featureResolver,
            onboardingConfigHolder,
            rootLayoutHolder,
            rootViewConfigHolder,
            currentProjectTagProvider,
            conditionChecker,
            testScope
        )

        assertDoesNotThrow {
            runTest {
                concreteRootLayoutResolver.setupRootLayout(mockk(), false)
                verify(exactly = 0, timeout = 2000) {
                    featureResolver.resolve(FeatureInfo(FeatureKey("name", 98), null))
                }
            }
        }
    }

    @Test
    fun updateRootLayoutWithNullRootViewConfig_shouldDoNothing() {
        val holder = mockkClass(RootLayoutHolder::class)
        val configHolder = mockkClass(RootViewConfigurationHolder::class)
        every { configHolder.value } returns null
        every { configHolder.flow } returns flowOf(null)

        val concreteRootLayoutManager = ConcreteRootLayoutManager(
            featureResolver,
            onboardingConfigHolder,
            holder,
            configHolder,
            currentProjectTagProvider,
            conditionChecker,
            testScope
        )

        runTest {
            concreteRootLayoutManager.updateRootLayout()
            verify(exactly = 0) {
                holder.setRootLayout(any())
            }
        }
    }
}
