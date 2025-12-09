package com.greencopper.interfacekit.multiproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greencopper.core.content.projectswitcher.ProjectParams
import com.greencopper.core.content.projectswitcher.ProjectSwitcher
import com.greencopper.interfacekit.multiproject.ProjectSwitchingLayoutData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class ProjectSwitchingViewModel(
    private val projectSwitcher: ProjectSwitcher,
    private val backgroundCoroutineContext: CoroutineContext
) : ViewModel() {

    private val _error: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    val error: StateFlow<Throwable?> = _error

    fun switchProject(project: ProjectSwitchingLayoutData.Project) {
        _error.value = null
        viewModelScope.launch(backgroundCoroutineContext) {
            try {
                projectSwitcher.switchProject(
                    ProjectParams(project.content.project, project.content.otaApiUrl)
                )
            } catch (throwable: Throwable) {
                _error.value = throwable
            }
        }
    }

    fun resetViewModel() {
        _error.value = null
    }
}
