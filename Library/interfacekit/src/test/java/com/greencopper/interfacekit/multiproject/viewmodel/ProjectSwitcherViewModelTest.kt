package com.greencopper.interfacekit.multiproject.viewmodel

import com.greencopper.interfacekit.multiproject.ProjectSwitcherLayoutData
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitcherAdapter
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitcherAdapter.ProjectItem
import com.greencopper.testmocks.core.MockCurrentProjectTagProvider
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ProjectSwitcherViewModelTest {
    private val currentProjectTagProvider = MockCurrentProjectTagProvider(
        currentProjectImpl = { "currentProject" },
    )
    private val classUnderTest = ProjectSwitcherViewModel(currentProjectTagProvider, MockTimezoneProvider())

    init {
        Toolkit.setupTest()
    }

    @Test
    fun setItems_whenSuccess() {
        val startDate1 = ZonedDateTime.now()
        classUnderTest.setItems(
            ProjectSwitcherLayoutData(
                projects = listOf(
                    ProjectSwitcherLayoutData.Project(
                        "name1",
                        date = ProjectSwitcherLayoutData.Project.ProjectDate(
                            start = startDate1.toString(),
                            end = "wrong date",
                        ),
                        "image",
                        content = ProjectSwitcherLayoutData.Project.Content(
                            project = "project",
                            otaApiUrl = "",
                        )
                    ),
                    ProjectSwitcherLayoutData.Project(
                        "name2",
                        date = null,
                        "image",
                        content = ProjectSwitcherLayoutData.Project.Content(
                            project = "project",
                            otaApiUrl = "",
                        )
                    ),
                    ProjectSwitcherLayoutData.Project(
                        "name3",
                        date = ProjectSwitcherLayoutData.Project.ProjectDate(
                            start = null,
                            end = null,
                        ),
                        "image",
                        content = ProjectSwitcherLayoutData.Project.Content(
                            project = "project",
                            otaApiUrl = "",
                        )
                    ),
                ),

            )
        )
        assertThat(classUnderTest.items)
            .usingRecursiveComparison()
            .isEqualTo(
                listOf(
                    ProjectSwitcherAdapter.HeaderItem(
                        title = "interfaceKit.project_switcher.title",
                        subtitle = "interfaceKit.project_switcher.subtitle"
                    ),
                    ProjectItem(
                        id = "project",
                        name = "name1",
                        startDate = startDate1,
                        endDate = null,
                        thumbnailUrl = "image",
                        ZoneId.systemDefault()
                    ),
                    ProjectItem(
                        id = "project",
                        name = "name2",
                        startDate = null,
                        endDate = null,
                        thumbnailUrl = "image",
                        ZoneId.systemDefault()
                    ),
                    ProjectItem(
                        id = "project",
                        name = "name3",
                        startDate = null,
                        endDate = null,
                        thumbnailUrl = "image",
                        ZoneId.systemDefault()
                    )
                )
            )
    }

    @Test
    fun canSwitchProject_whenTrue() {
        classUnderTest.selectedItemId = "test1"
        assertThat(classUnderTest.canSwitchProject()).isTrue
    }


    @Test
    fun canSwitchProject_whenFalse() {
        classUnderTest.selectedItemId = "currentProject"
        assertThat(classUnderTest.canSwitchProject()).isFalse
    }

    @Test
    fun getItems_whenNotInitialized() {
        assertThrows<UninitializedPropertyAccessException> {
            classUnderTest.items
        }
    }
}
