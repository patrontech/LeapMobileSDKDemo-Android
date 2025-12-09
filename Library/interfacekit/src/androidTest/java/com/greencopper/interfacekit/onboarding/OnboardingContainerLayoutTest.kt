package com.greencopper.interfacekit.onboarding

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.fragment.app.testing.withFragment
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.maincard.MainActionCardLayoutData
import com.greencopper.interfacekit.onboarding.maincard.MainActionCardPageInitializer
import com.greencopper.interfacekit.onboarding.maincard.ui.MainActionCardFragment
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.interfacekit.test.R
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class OnboardingContainerLayoutTest {

    private val pageLayoutData = MainActionCardLayoutData(
        title = "",
        text = "",
        backgroundImage = "",
        onboardingPageLayoutData = OnboardingPageLayoutData(
            "pageId",
            OnboardingPageLayoutData.OnboardingAnalytics("", "")
        )
    )

    private val mockOnboardingController: OnboardingController = MockOnboardingController()
    private val mockMetricsService: AggregateMetricsService = mockk()
    private val mockLocalizationService: LocalizationService = mockk()
    private val mockImageService = MockImageService()
    private val mockColorRepository: ColorRepository = MockColorRepository()

    init {
        Toolkit.setupTest(applicationContext = InstrumentationRegistry.getInstrumentation().context)
        bindProvider(mockOnboardingController)
        bindSingleton<ColorRepository>(mockColorRepository)
        bindSingleton<TextStyleRepository>(MockTextStyleRepository())
        bindProvider<ImageService>(mockImageService)
        every { mockLocalizationService.getString(any()) } returns ""
        bindProvider<LocalizationService>(mockLocalizationService)
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())

        bindSingleton(mockMetricsService)
        every { mockMetricsService.track(any()) } returns Unit
    }

    @Test
    @DisplayName("Given a valid layout, When calling display, Then track function is called")
    @Disabled("Cannot resolve ColorRepository in this test")
    fun displayShouldCallTrack() {
        val scenario = launchFragmentInContainer(themeResId = R.style.TestTheme) {
            OnboardingContainerLayout.newInstance(
                OnboardingContext(
                    redirectionHash = null,
                    pages = listOf(
                        OnboardingPageInfo(
                            id = "pageId",
                            key = MainActionCardPageInitializer.key,
                        )
                    ),
                    isAppOnboarding = false,
                )
            )
        }

        scenario.withFragment {
            val newPage = MainActionCardFragment(pageLayoutData)
            display(newPage)
            verify { mockMetricsService.track(any()) }
        }
    }

    @Test
    @DisplayName("Given a valid layout, When fragment is recreated, Then data is restored")
    fun recreateShouldSucceed() {
        val onboardingContext = OnboardingContext(
            redirectionHash = null,
            pages = listOf(
                OnboardingPageInfo(
                    id = "pageId",
                    key = MainActionCardPageInitializer.key,
                ),
            ),
            isAppOnboarding = true,
        )

        val bundle = Bundle()
        bundle.putInt(OnboardingContainerLayout.ONBOARDING_CONTEXT_ARG_KEY, onboardingContext.hashCode())

        val scenario = launchFragmentInContainer(bundle) {
            OnboardingContainerLayout.newInstance(
                onboardingContext
            )
        }.recreate()

        scenario.withFragment {
            assertThat(isAdded).isTrue
        }
    }
}
