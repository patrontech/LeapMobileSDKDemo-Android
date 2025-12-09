package com.greencopper.core.content.manager

import com.greencopper.toolkit.storage.StorageManager

internal class ConcreteProjectCleaner(private val storageManager: StorageManager) : ProjectCleaner {
    override suspend fun cleanProjectsData(projectsToKeep: Set<String>) {
        val regex = Regex("[a-zA-Z0-9]*-[0-9]{4}")
        val projectsFound = storageManager.getFilesStorage()
            .listFiles { file ->
                file.isDirectory && regex.matches(file.name)
            }
        val projectsToRemove = projectsFound?.filterNot { projectsToKeep.contains(it.name) }
        projectsToRemove?.forEach { storageManager.deleteFile(it) }
    }
}
