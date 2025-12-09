package com.greencopper.interfacekit.onboarding

import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.*
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout.OnboardingControllerException
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.closePresentedLayout
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockOnboardingPageInitializer
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ConcreteOnboardingControllerTest {

    private lateinit var classUnderTest: ConcreteOnboardingController
    private val mockRouteController: RouteController = mockk()
    private val mockFeatureResolver: FeatureResolver = mockk()
    private val mockConditionChecker: ConditionChecker = mockk()
    private val mockAggregateMetricsService: AggregateMetricsService = mockk()
    private val onboardingPageKey = OnboardingPageKey("name", 1)
    private val logging = MockLogging()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        bindProvider<OnboardingPageInitializer>(MockOnboardingPageInitializer(), onboardingPageKey)
    }

    @Test
    @DisplayName("Given context is valid, When setup is called, Then setup returns the onboarding container layout")
    fun setupShouldReturnOnboardingPageLayout() {
        val pages = listOf(
            OnboardingPageInfo(
                id = "id",
                key = onboardingPageKey,
            )
        )
        every { mockAggregateMetricsService.track(any()) } returns Unit

        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = pages,
                isAppOnboarding = false,
            ),
            logging,
        )
        assertThat(classUnderTest.getLayoutToDisplay()).isInstanceOf(OnboardingPageLayout::class.java)
    }

    @Test
    @DisplayName("Given context is valid and no page could be resolved, When setup is called, Then setup returns the feature layout")
    fun setupShouldReturnFeatureLayout() {
        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = emptyList(),
                feature = mockk(),
                isAppOnboarding = false,
            ),
            logging,
        )
        mockBundleConstructor()
        every { mockRouteController.replace(any(), any()) } returns Unit
        every { mockFeatureResolver.resolve(any()) } returns mockk()
        mockkStatic("com.greencopper.interfacekit.navigation.layout.LayoutKt")
        assertThat(classUnderTest.getLayoutToDisplay()).isInstanceOf(Layout::class.java)
    }

    @Test
    @DisplayName("Given context is valid and no page or feature could be resolved, When setup is called, Then setup throws NoPageNoFeatureException")
    fun setupShouldThrowNoPageNoFeatureException() {
        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = emptyList(),
                isAppOnboarding = false,
            ),
            logging,
        )

        assertThrows<OnboardingControllerException.NoPageNoFeatureException> {
            classUnderTest.getLayoutToDisplay()
        }
    }

    @Test
    @DisplayName("Given current page is completed, When page did complete is called, Then no exception is thrown")
    fun pageDidCompleteShouldDisplayNextPage() {
        val pages = listOf(
            OnboardingPageInfo(
                id = "id1",
                key = onboardingPageKey,
            ),
            OnboardingPageInfo(
                id = "id2",
                key = onboardingPageKey,
            )
        )
        every { mockAggregateMetricsService.track(any()) } returns Unit
        App.resolve<LocalStorage>().project.interfaceKit.onboarding.completedPages.value = emptySet()

        mockkStatic(Layout::closePresentedLayout)
        val containerLayout = mockk<OnboardingContainerLayout>(relaxed = true)
        every { containerLayout.closePresentedLayout() } just runs

        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = pages,
                isAppOnboarding = false,
            ),
            logging,
        )
        classUnderTest.getLayoutToDisplay()
        assertDoesNotThrow {
            classUnderTest.pageDidComplete(
                containerLayout,
                pages.first().id,
                true
            )
        }
    }

    @Test
    @DisplayName("Given onboarding is completed, When page did complete is called, Then no exception is thrown")
    fun pageDidCompleteShouldDisplayFeature() {
        val pages = listOf(
            OnboardingPageInfo(
                id = "id1",
                key = onboardingPageKey,
            ),
            OnboardingPageInfo(
                id = "id2",
                key = onboardingPageKey,
            )
        )
        every { mockAggregateMetricsService.track(any()) } returns Unit
        App.resolve<LocalStorage>().project.interfaceKit.onboarding.completedPages.value = emptySet()
        every { mockRouteController.resolve(any(), any()) } returns Unit
        every { mockRouteController.replace(any(), any()) } returns Unit

        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = pages,
                feature = mockk(),
                isAppOnboarding = false,
            ),
            logging,
        )
        classUnderTest.getLayoutToDisplay()
        classUnderTest.pageDidComplete(OnboardingContainerLayout(), pages.last().id, true)
        verify { mockRouteController.replace(any(), any()) }
    }

    @Test
    @DisplayName("Given page id is null, When after is called, Then it returns the whole list")
    fun afterShouldReturnAllItems() {
        val pages = listOf(
            OnboardingPageInfo(
                id = "id1",
                key = onboardingPageKey,
            ),
            OnboardingPageInfo(
                id = "id2",
                key = onboardingPageKey,
            )
        )

        assertThat(pages.after(null).size).isEqualTo(2)
    }

    @Test
    @DisplayName("Given pages list is empty, When after is called, Then it returns empty list")
    fun afterShouldReturnEmptyList() {
        val pages = emptyList<OnboardingPageInfo>()

        assertThat(pages.after(null).size).isEqualTo(0)
    }

    @Test
    @DisplayName("Given page id is not found, When after is called, Then it returns empty list")
    fun afterIdNotFoundShouldReturnEmptyList() {
        val pages = listOf(
            OnboardingPageInfo(
                id = "id1",
                key = onboardingPageKey,
            ),
            OnboardingPageInfo(
                id = "id2",
                key = onboardingPageKey,
            )
        )

        assertThat(pages.after("wrong_id").size).isEqualTo(0)
    }

    @Test
    fun givenPagesNotInSequence_onboardingSequence_isSmaller() {
        val notInSequenceKey = OnboardingPageKey("notInSequence", 1)
        val onboardingInitializer = MockOnboardingPageInitializer(showInSequence = false)
        bindProvider<OnboardingPageInitializer>(onboardingInitializer, notInSequenceKey)

        val pages = listOf(
            OnboardingPageInfo(
                id = "id1",
                key = onboardingPageKey,
            ),
            OnboardingPageInfo(
                id = "id2",
                key = notInSequenceKey
            ),
            OnboardingPageInfo(
                id = "id3",
                key = onboardingPageKey,
            ),
        )

        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = pages,
                isAppOnboarding = false,
            ),
            logging,
        )

        assertThat(classUnderTest.onboardingSequence.pages.size).isEqualTo(pages.size - 1)
    }

    @Test
    fun sequence_toViewData_returnsSelectedPage() {
        val sequence = OnboardingSequence(listOf(
            OnboardingPageInfo("1", onboardingPageKey),
            OnboardingPageInfo("2", onboardingPageKey),
            OnboardingPageInfo("3", onboardingPageKey),
        ))

        val result = sequence.toViewData("2")

        result.selectedPage shouldBe 1
        result.numberOfPages shouldBe 3
    }

    @Test
    fun sequence_toViewData_returnsSelectedNoPage() {
        val sequence = OnboardingSequence(
            listOf(
                OnboardingPageInfo("1", onboardingPageKey),
                OnboardingPageInfo("2", onboardingPageKey),
                OnboardingPageInfo("3", onboardingPageKey),
            )
        )

        val result = sequence.toViewData("other")

        result.selectedPage shouldBe -1
        result.numberOfPages shouldBe 3
    }

    @Test
    fun givenContextWithInvalidKey_getLayoutToDisplay_throwsNoPageNoFeatureException() {
        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = listOf(OnboardingPageInfo("id", OnboardingPageKey("Invalid.Unknown", 44))),
                isAppOnboarding = false,
            ),
            logging,
        )

        assertThrows<OnboardingControllerException.NoPageNoFeatureException> {
            classUnderTest.getLayoutToDisplay()
        }
    }

    @Test
    fun givenContextWithInvalidKey_onboardingSequence_doesNotThrow() {
        classUnderTest = ConcreteOnboardingController(
            mockRouteController,
            mockFeatureResolver,
            mockConditionChecker,
            LazyResolver.adhoc(LocalStorage("project")),
            OnboardingContext(
                redirectionHash = mockk(),
                pages = listOf(OnboardingPageInfo("id", OnboardingPageKey("Invalid.Unknown", 44))),
                isAppOnboarding = false,
            ),
            logging,
        )

        assertDoesNotThrow {
            classUnderTest.onboardingSequence
        }
    }
}
