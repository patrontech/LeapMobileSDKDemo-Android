package com.greencopper.interfacekit.multiproject

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.fullscreenmedia.*
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitchingFragment
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.*
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ProjectSwitchingInitializerTest {
    private lateinit var classUnderTest: ProjectSwitchingInitializer

    private val validParams = ProjectSwitchingData(
        project = ProjectSwitchingData.Project(
            name = "New Project",
            image = "image_url",
            content = ProjectSwitchingData.Project.Content(
                project = "new_project",
                otaApiUrl = "https://www.ota_api_url.com"
            )
        ),
        ScreenNameAnalytics(
            screenName = "screenName"
        )
    )

    @BeforeEach
    fun setupEach() {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        classUnderTest = ProjectSwitchingInitializer()
    }

    @Test
    @DisplayName("Given params are valid, When getLayout is called, Then a layout is returned")
    fun getLayoutShouldSucceed() {
        mockBundleConstructor()
        assertThat(classUnderTest.getLayout(validParams.encodeToJsonElement())).isNotNull
    }

    @Test
    @DisplayName("Given params are null, When getLayout is called, Then NoParametersProvidedError is thrown")
    fun getLayoutShouldThrowNoParametersProvidedError() {
        val exception =
            assertThrows<FeatureInitializerException.NoParametersProvidedException> {
                classUnderTest.getLayout(null)
            }
        assertThat(exception.message).isNotNull
    }

    @Test
    @DisplayName("Given params are not deserializable, When getLayout is called, Then DecodingFailedException is thrown")
    fun getLayoutShouldThrowDecodingFailedException() {
        val exception =
            assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
                classUnderTest.getLayout(JsonNull)
            }
        assertThat(exception.message).isNotNull
    }

    @Test
    @DisplayName("Given params are not invalid, When getLayout is called, Then InvalidParametersException is thrown")
    fun getLayoutWithInvalidParamsShouldThrowDecodingFailedException() {
        val exception =
            assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
                val params = FullScreenMediaLayoutData(
                    "path",
                    ScreenNameAnalytics("analytics"),
                    RedirectionHash(FullScreenMediaInitializer.key)
                ).encodeToJsonElement()
                classUnderTest.getLayout(params)
            }
        assertThat(exception.message).isNotNull
    }

    @Test
    @DisplayName("Given params are valid, When getRedirectionHash is called, Then a redirection hash is returned")
    fun getRedirectionHashWithCorrectParamsShouldSucceed() {
        val params = validParams.encodeToJsonElement()
        val hash = classUnderTest.redirectionHashFor(params)
        assertThat(hash).isNotNull
    }

    @Nested
    @DisplayName("Given onboarding params are valid")
    inner class ValidOnboardingParams {
        private val paramsWithOnboarding = validParams.copy(
            onboardingAnalytics = OnboardingPageLayoutData.OnboardingAnalytics(
                "projectSwitching", "Home"
            )
        )

        @Test
        @DisplayName("When getLayout is called, Then layout is returned")
        fun getLayoutShouldSucceed() {
            mockBundleConstructor()
            assertThat((classUnderTest.getLayout(paramsWithOnboarding.encodeToJsonElement()))).isInstanceOf(
                ProjectSwitchingFragment::class.java
            )
        }

        @Test
        @DisplayName("When redirectionHashFor is called, Then a redirection hash is returned")
        fun redirectionHashForShouldSucceed() {
            assertThat(classUnderTest.redirectionHashFor(paramsWithOnboarding.encodeToJsonElement())).isNotNull
        }

        @Test
        @DisplayName("When resolve is called, Then a layout is returned")
        fun resolveForShouldSucceed() {
            val pageId = "projectSwitcher"
            assertThat(
                classUnderTest.resolve(
                    paramsWithOnboarding.encodeToJsonElement(),
                    pageId
                )
            ).isInstanceOf(
                ProjectSwitchingFragment::class.java
            )
        }
    }

    @Test
    @DisplayName("Given null onboarding params, When resolve is called, Then IllegalStateException is thrown")
    fun resolveForShouldSucceed() {
        val pageId = "projectSwitcher"
        assertThrows<IllegalStateException> {
            classUnderTest.resolve(
                null,
                pageId
            )
        }
    }

    @Test
    fun showInSequence_returnsTrue() {
        assertThat(classUnderTest.showInSequence()).isTrue
    }
}
