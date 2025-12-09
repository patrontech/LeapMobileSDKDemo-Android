package com.greencopper.interfacekit.multiproject.viewmodel

import com.greencopper.core.content.projectswitcher.ProjectParams
import com.greencopper.interfacekit.multiproject.ProjectSwitchingLayoutData
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.interfacekit.MockProjectSwitcher
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ProjectSwitchingViewModelTest : CoroutineTest(UnconfinedTestDispatcher()) {
    private val projectSwitcher = MockProjectSwitcher()
    private val classUnderTest =
        ProjectSwitchingViewModel(projectSwitcher, testScope.coroutineContext)

    @Test
    fun switchProject_whenSuccess() {
        projectSwitcher.mockContent = mockk(relaxed = true)
        classUnderTest.switchProject(
            ProjectSwitchingLayoutData.Project(
                name = "name",
                content = ProjectSwitchingLayoutData.Project.Content(
                    project = "project",
                    otaApiUrl = "otaApiUrl",
                )
            )
        )
        assertThat(classUnderTest.error.value).isNull()
        assertThat(projectSwitcher.lastSwitchProject).isEqualTo(
            ProjectParams(
                project = "project",
                otaApiUrl = "otaApiUrl",
            )
        )
    }

    @Test
    fun switchProject_whenException() {
        val exception = RuntimeException("Failed to switch")
        projectSwitcher.mockSwitchProjectException = exception
        classUnderTest.switchProject(
            ProjectSwitchingLayoutData.Project(
                name = "name",
                content = ProjectSwitchingLayoutData.Project.Content(
                    project = "project",
                    otaApiUrl = "otaApiUrl",
                )
            )
        )
        assertThat(classUnderTest.error.value)
            .isEqualTo(exception)

        classUnderTest.resetViewModel()
        assertThat(classUnderTest.error.value)
            .isNull()
    }

    override fun afterEach() {}
}
