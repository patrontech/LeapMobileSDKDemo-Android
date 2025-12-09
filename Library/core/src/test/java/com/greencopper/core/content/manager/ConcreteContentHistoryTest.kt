package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockStorageManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class ConcreteContentHistoryTest {
    private companion object {
        private const val PROJECT = "project"
        private const val SECRET = "abc123"
    }
    private val archive = ContentArchive(
        File("archive.zip"),
        SECRET,
    )
    private val content: Content
        by lazy { Content(archive, 1, 1, PROJECT, OTAContent.Type.Release) }
    private val contentDirectory = File("contents")
    private val storageManager = MockStorageManager(
        filesStorage = { contentDirectory }
    )
    private lateinit var json: Json

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        json = App.resolve()
        contentDirectory.mkdir()
    }

    @AfterEach
    fun afterEach() {
        contentDirectory.deleteRecursively()
    }

    @Test
    fun whenHistoryIsSaved_itIsPersisted() {
        runTest {
            val writtenHistory = ConcreteContentHistory(storageManager, json)
            writtenHistory.saveContent(content)
            writtenHistory.currentContent = content
            writtenHistory.forcedContent = content

            val readHistory = ConcreteContentHistory(storageManager, json)
            assertThat(readHistory.contents.first()).isEqualTo(content)
            assertThat(readHistory.currentProject).isEqualTo(PROJECT)
            assertThat(readHistory.forcedContent).isEqualTo(content)
        }
    }

    @Test
    fun whenCurrentContentIsChanged_currentContentFlowEmits() = runTest {
        val history = ConcreteContentHistory(storageManager, json)
        history.saveContent(content)
        history.currentContent = content
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            history.currentContentFlow.collect {
                assertThat(it).isEqualTo(content)
            }
        }
    }

    @Test
    fun whenCurrentContentIsChanged_currentProjectFlowEmits() = runTest {
        val history = ConcreteContentHistory(storageManager, json)
        history.saveContent(content)
        history.currentContent = content
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            history.currentProjectFlow.collect {
                assertThat(it).isEqualTo(PROJECT)
            }
        }
    }
}
