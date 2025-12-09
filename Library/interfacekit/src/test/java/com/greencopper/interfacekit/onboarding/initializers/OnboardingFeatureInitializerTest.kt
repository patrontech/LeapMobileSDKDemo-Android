package com.greencopper.interfacekit.onboarding.initializers

import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.onboarding.OnboardingController
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.testmocks.mockBundleConstructor
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.Resolver
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class OnboardingFeatureInitializerTest {

    private lateinit var classUnderTest: OnboardingFeatureInitializer
    private val mockResolver: Resolver = mockk()
    private val mockOnboardingController: OnboardingController = mockk()

    init {
        Toolkit.setupTest()
    }

    @BeforeEach
    fun setupEach() {
        classUnderTest = OnboardingFeatureInitializer(mockResolver)
    }

    @Nested
    @DisplayName("Given params are valid")
    inner class ValidParams {

        private val params = OnboardingFeatureData(
            pages = listOf(
                OnboardingPageInfo(
                    id = "page1",
                    key = OnboardingPageKey(
                        name = "Onboarding",
                        version = 1
                    ),
                    null,
                    null
                )
            ),
            redirectionId = "Onboarding"
        ).encodeToJsonElement()

        @Test
        @DisplayName("When getLayout is called, Then layout is returned")
        fun getLayoutShouldSucceed() {
            mockBundleConstructor()
            val onboardingPageMainCardLayout = OnboardingContainerLayout()
            every { mockOnboardingController.getLayoutToDisplay() } returns onboardingPageMainCardLayout
            every {
                mockResolver.resolve(OnboardingController::class, any(), any())
            } returns Pair(
                Key(OnboardingController::class, Unit),
                mockOnboardingController
            )
            assertThat(classUnderTest.getLayout(params)).isInstanceOf(OnboardingContainerLayout::class.java)
        }

        @Test
        @DisplayName("When redirectionHashFor is called, Then a redirection hash is returned")
        fun redirectionHashForShouldSucceed() {
            assertThat(classUnderTest.redirectionHashFor(params)).isNotNull
        }
    }

    @Nested
    @DisplayName("Given params are null")
    inner class NullParams {
        @Test
        @DisplayName("When getLayout is called, Then NoParametersProvidedException is raised")
        fun getLayoutShouldThrow() {
            every {
                mockResolver.resolve(OnboardingController::class, any(), any())
            } returns Pair(
                Key(OnboardingController::class, Unit),
                mockOnboardingController
            )

            assertThrows<FeatureInitializerException.NoParametersProvidedException> {
                classUnderTest.getLayout(null)
            }
        }

        @Test
        @DisplayName("When redirectionHashFor is called, Then a default hash is returned")
        fun redirectionHashForShouldReturnDefault() {
            assertThat(classUnderTest.redirectionHashFor(null))
                .isEqualTo(RedirectionHash(OnboardingFeatureInitializer.key))
        }
    }

    @Nested
    @DisplayName("Given params are invalid")
    inner class InvalidParams {

        private val params = JsonNull

        @Test
        @DisplayName("When getLayout is called, Then ParametersNotValid is raised")
        fun getLayoutShouldThrow() {
            assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
                classUnderTest.getLayout(params)
            }
        }

        @Test
        @DisplayName("When redirectionHashFor is called, Then a default hash is returned")
        fun redirectionHashForShouldReturnDefault() {
            assertThat(classUnderTest.redirectionHashFor(params))
                .isEqualTo(RedirectionHash(OnboardingFeatureInitializer.key))
        }
    }

    @Nested
    @DisplayName("Given params are valid but no controller is resolved")
    inner class ValidParamsButNoController {
        private val params = OnboardingFeatureData(
            pages = listOf(
                OnboardingPageInfo(
                    id = "page1",
                    key = OnboardingPageKey(
                        name = "Onboarding",
                        version = 1
                    ),
                    null,
                    null
                )
            ),
            redirectionId = "Onboarding"
        ).encodeToJsonElement()

        @Test
        @DisplayName("When getLayout is called, Then exception is thrown")
        fun getLayoutShouldSucceed() {
            val onboardingPageMainCardLayout = OnboardingContainerLayout()
            every { mockOnboardingController.getLayoutToDisplay() } returns onboardingPageMainCardLayout
            every {
                mockResolver.resolve(OnboardingController::class, any(), any())
            } returns Pair(
                Key(OnboardingController::class, Unit),
                null
            )

            assertThrows<FeatureInitializerException.ParametersNotValid> {
                classUnderTest.getLayout(params)
            }
        }

        @Test
        @DisplayName("When redirectionHashFor is called, Then a redirection hash is returned")
        fun redirectionHashForShouldSucceed() {
            assertThat(classUnderTest.redirectionHashFor(params)).isNotNull
        }
    }
}
