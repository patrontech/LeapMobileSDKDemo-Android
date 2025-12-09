package com.greencopper.interfacekit.multiproject.viewmodel

import androidx.lifecycle.ViewModel
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.interfacekit.multiproject.ProjectSwitcherLayoutData
import com.greencopper.interfacekit.multiproject.ui.ProjectSwitcherAdapter
import com.greencopper.toolkit.extensions.toZonedDateTime

internal class ProjectSwitcherViewModel(
    private val projectTagProvider: CurrentProjectTagProvider,
    private val timezoneProvider: TimezoneProvider,
) : ViewModel(), ProjectSwitcherAdapter.ItemProvider {

    override lateinit var items: List<ProjectSwitcherAdapter.Item>

    override var selectedItemId: String? = projectTagProvider.currentProject

    fun setItems(data: ProjectSwitcherLayoutData) {
        items = ArrayList<ProjectSwitcherAdapter.Item>().apply {
            val title = "interfaceKit.project_switcher.title"
            val subtitle = "interfaceKit.project_switcher.subtitle"
            add(ProjectSwitcherAdapter.HeaderItem(title, subtitle))

            addAll(
                data.projects
                    .map {
                        val startDate = it.date?.start?.toZonedDateTime()
                        val endDate = it.date?.end?.toZonedDateTime()
                        ProjectSwitcherAdapter.ProjectItem(
                            id = it.content.project,
                            name = it.name,
                            startDate = startDate,
                            endDate = endDate,
                            thumbnailUrl = it.image,
                            zoneId = timezoneProvider.zoneId
                        )
                    }
            )
        }
    }

    fun canSwitchProject() = projectTagProvider.currentProject != selectedItemId
}
