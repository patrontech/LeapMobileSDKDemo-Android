package com.greencopper.core.content.manager

import com.greencopper.testmocks.MockFile
import com.greencopper.testmocks.toolkit.MockStorageManager
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteProjectCleanerTest {

    private lateinit var projectCleaner: ConcreteProjectCleaner
    private lateinit var storageManager: MockStorageManager

    @Test
    fun cleanProjectsDataWithEmptySetShouldDeleteTwoProjects() {
        runTest {
            val projectDir1 = MockFile(isDirectoryValue = true, fileName = "project_1")
            val projectDir2 = MockFile(isDirectoryValue = true, fileName = "project_2")
            storageManager = MockStorageManager(filesStorage = {
                MockFile(
                    listFilesValue = arrayOf(
                        projectDir1,
                        projectDir2
                    ), isDirectoryValue = true
                )
            })
            projectCleaner = ConcreteProjectCleaner(storageManager)
            projectCleaner.cleanProjectsData(emptySet())
            assertThat(storageManager.deleteFileCallCount).isEqualTo(2)
        }
    }

    @Test
    fun cleanProjectsDataWithSetShouldDeleteOneProject() {
        runTest {
            val projectDir1 = MockFile(isDirectoryValue = true, fileName = "project_1")
            val projectDir2 = MockFile(isDirectoryValue = true, fileName = "project_2")
            storageManager = MockStorageManager(filesStorage = {
                MockFile(
                    listFilesValue = arrayOf(
                        projectDir1,
                        projectDir2
                    ), isDirectoryValue = true
                )
            })
            projectCleaner = ConcreteProjectCleaner(storageManager)
            projectCleaner.cleanProjectsData(setOf("project_1"))
            assertThat(storageManager.deleteFileCallCount).isEqualTo(1)
        }
    }

    @Test
    fun cleanProjectsDataWithKeepProjectsShouldDeleteTwoProjects() {
        runTest {
            val projectDir1 = MockFile(isDirectoryValue = true, fileName = "project_1")
            val projectDir2 = MockFile(isDirectoryValue = true, fileName = "project_2")
            storageManager = MockStorageManager(filesStorage = {
                MockFile(
                    listFilesValue = arrayOf(
                        projectDir1,
                        projectDir2
                    ), isDirectoryValue = true
                )
            })
            projectCleaner = ConcreteProjectCleaner(storageManager)
            projectCleaner.cleanProjectsData(setOf("project_1", "project_2"))
            assertThat(storageManager.deleteFileCallCount).isEqualTo(0)
        }
    }

    @Test
    fun cleanProjectsDataWithNoDirectories() {
        runTest {
            val projectDir1 = MockFile(isDirectoryValue = false, fileName = "project_1")
            val projectDir2 = MockFile(isDirectoryValue = false, fileName = "project_2")
            storageManager = MockStorageManager(filesStorage = {
                MockFile(
                    listFilesValue = arrayOf(
                        projectDir1,
                        projectDir2
                    ), isDirectoryValue = true
                )
            })
            projectCleaner = ConcreteProjectCleaner(storageManager)
            projectCleaner.cleanProjectsData(setOf("project_1", "project_2"))
            assertThat(storageManager.deleteFileCallCount).isEqualTo(0)
        }
    }
}
