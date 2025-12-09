package com.greencopper.interfacekit.multiproject

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitcherFragment
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayoutData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class ProjectSwitcherInitializerTest {

    private lateinit var classUnderTest: ProjectSwitcherInitializer

    @BeforeEach
    fun setupEach() {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        mockBundleConstructor()
        classUnderTest = ProjectSwitcherInitializer()
    }

    @Nested
    @DisplayName("Given params are valid")
    inner class ValidParams {
        private val sampleProject = ProjectSwitcherData.Project(
            name = "project_1",
            date = ProjectSwitcherData.Project.ProjectDate(null, null),
            image = "image_url",
            content = ProjectSwitcherData.Project.Content(
                "project",
                "https://www.google.com"
            )
        )
        private val projects = listOf(
            sampleProject,
            sampleProject.copy(
                name = "project_2",
                content = ProjectSwitcherData.Project.Content(
                    project = "project_2",
                    otaApiUrl = "https://www.google.ca"
                )
            ),
            sampleProject.copy(
                name = "project_3",
                content = ProjectSwitcherData.Project.Content(
                    project = "project_2",
                    otaApiUrl = "https://www.google.fr"
                )
            )
        )

        val params = ProjectSwitcherData(
            projects = projects,
            analytics = ScreenNameAnalytics("multiProjectSwitcher")
        )


        @Test
        @DisplayName("When getLayout is called, Then layout is returned")
        fun getLayoutShouldSucceed() {
            assertThat((classUnderTest.getLayout(params.encodeToJsonElement()))).isInstanceOf(
                ProjectSwitcherFragment::class.java
            )
        }

        @Test
        @DisplayName("When redirectionHashFor is called, Then a redirection hash is returned")
        fun redirectionHashForShouldSucceed() {
            assertThat(classUnderTest.redirectionHashFor(params.encodeToJsonElement())).isNotNull
        }

        @Nested
        @DisplayName("And onboarding params are valid")
        inner class ValidOnboardingParams {
            private val paramsWithOnboarding = params.copy(
                onboardingAnalytics = OnboardingPageLayoutData.OnboardingAnalytics(
                    "projectSwitcher", "Home"
                )
            )

            @Test
            @DisplayName("When getLayout is called, Then layout is returned")
            fun getLayoutShouldSucceed() {
                assertThat((classUnderTest.getLayout(paramsWithOnboarding.encodeToJsonElement()))).isInstanceOf(
                    ProjectSwitcherFragment::class.java
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
                    ProjectSwitcherFragment::class.java
                )
            }

            @Test
            @DisplayName("When resolve is called, Then a layout is returned")
            fun resolveForWithNoParamsShouldFail() {
                val pageId = "projectSwitcher"
                assertThrows<IllegalStateException> {
                    classUnderTest.resolve(
                        null, pageId
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("Given params are null")
    inner class NullParams {
        @Test
        @DisplayName("When getLayout is called, Then NoParametersProvidedException is raised")
        fun getLayoutShouldThrow() {
            assertThrows<FeatureInitializerException.NoParametersProvidedException> {
                classUnderTest.getLayout(null)
            }
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
    }

    @Test
    fun showInSequence_returnsTrue() {
        assertThat(classUnderTest.showInSequence()).isTrue
    }
}
