package com.greencopper.core.content.ota

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.manager.State
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.core.MockDraftContentManager
import com.greencopper.testmocks.core.MockOTARepository
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class ConcreteOTAManagerTest: CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val otaRepository = MockOTARepository(
        getContentsValue = { emptyList() },
        getArchiveFileValue = { File("") },
    )

    private val contentConfig = RunConfiguration.Content(
        secret = "secret",
        schema = 1,
        version = 1,
        project = "project"
    )
    private val defaultContent = Content(
        ContentArchive(File(""), "secret"),
        1,
        1,
        "project",
        OTAContent.Type.Release,
    )
    private val draftContent = Content(
        ContentArchive(File(""), "secret"),
        1,
        1,
        "project",
        OTAContent.Type.Draft,
    )
    private val draftContentManager = MockDraftContentManager(
        passcodeReturnValue = { null }
    )

    private val contentManager = MockContentManager()
    private val classUnderTest = ConcreteOTAManager(
        otaRepository,
        contentManager,
        draftContentManager,
        contentConfig,
        "",
        MockLogging()
    )

    override fun afterEach() {}

    @Nested
    @DisplayName("Given content to process is found")
    inner class ValidContentToProcess {
        private val otaContent = OTAContent(
            project = contentConfig.project,
            version = 3,
            typeString = "release",
            schema = 1
        )

        @BeforeEach
        internal fun setUp() {
            otaContent
            otaRepository.getContentsValue = { listOf(otaContent) }
            contentManager.eligibleContentsToApplyValue = { setOf(defaultContent) }
            contentManager.processValue = {
                defaultContent.apply {
                    currentState = State.Processed(
                        emptySet()
                    )
                }
            }
            contentManager.applyValue = { _, _ ->
                defaultContent.apply {
                    currentState = State.Applied(
                        emptySet()
                    )
                }
            }
        }

        @Test
        @DisplayName("When availableOtaContents is called, Then it should return the valid ota")
        fun availableOtaContentsShouldReturnOTAContent() {
            runTest {
                assertThat(
                    classUnderTest.availableOTAContents().single().version
                ).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When otaContentToProcess is called, Then it should return the valid ota")
        fun otaContentToProcessShouldReturnOTAContent() {
            runTest {
                assertThat(classUnderTest.otaContentToProcess()!!.version).isEqualTo(3)
            }
        }

        @Test
        @DisplayName("When process is called, Then it should return the valid content")
        fun processShouldReturnContent() {
            runTest {
                assertThat(classUnderTest.process(otaContent).currentState).isInstanceOf(
                    State.Processed::class.java
                )
            }
        }

        @Test
        @DisplayName("When force is called, Then it should return the valid content")
        fun forceShouldReturnContent() {
            runTest {
                assertThat(classUnderTest.force(otaContent).currentState).isInstanceOf(
                    State.Applied::class.java
                )
            }
        }
    }

    @Nested
    @DisplayName("Given content to process is not found")
    inner class NoContentToProcess {

        @BeforeEach
        internal fun setUp() {
            otaRepository.getContentsValue = { emptyList() }
            contentManager.eligibleContentsToApplyValue = { setOf(defaultContent) }
            contentManager.processValue = {
                defaultContent.apply {
                    currentState = State.Processed(
                        emptySet()
                    )
                }
            }
            contentManager.applyValue = { _, _ ->
                defaultContent.apply {
                    currentState = State.Applied(
                        emptySet()
                    )
                }
            }
        }

        @Test
        @DisplayName("When availableOtaContents is called, Then it should return an empty list")
        fun availableOtaContentsShouldReturnEmptyList() {
            runTest {
                assertThat(
                    classUnderTest.availableOTAContents()
                ).isEmpty()
            }
        }

        @Test
        @DisplayName("When otaContentToProcess is called, Then it should return null")
        fun otaContentToProcessShouldReturnOTAContent() {
            runTest {
                assertThat(classUnderTest.otaContentToProcess()).isNull()
            }
        }
    }

    @Test
    @DisplayName("Given an OTA has a greater version than content to apply, When otaContentToProcess is called, Then it should return the valid ota")
    fun otaContentToProcessShouldReturnOTAContent() {
        val otaContent = OTAContent(
            project = contentConfig.project,
            version = 3,
            typeString = "release",
            schema = 1
        )
        otaRepository.getContentsValue = { listOf(otaContent) }
        contentManager.eligibleContentsToApplyValue = {
            setOf(defaultContent.apply {
                currentState = State.Processed(
                    emptySet()
                )
            })
        }

        runTest {
            assertThat(classUnderTest.otaContentToProcess()!!.version).isEqualTo(3)
        }
    }

    @Test
    @DisplayName("Given an OTA has a inferior version than content to apply, When otaContentToProcess is called, Then it should return null")
    fun otaContentToProcessShouldNull() {
        val otaContent = OTAContent(
            project = contentConfig.project,
            version = 1,
            typeString = "release",
            schema = 1
        )
        otaRepository.getContentsValue = { listOf(otaContent) }
        contentManager.eligibleContentsToApplyValue = {
            setOf(defaultContent.apply {
                currentState = State.Processed(
                    emptySet()
                )
            })
        }

        runTest {
            assertThat(classUnderTest.otaContentToProcess()).isNull()
        }
    }

    @Test
    @DisplayName("Given draft OTA content, When otaContentToProcess is called with no password, Then it should return null")
    fun draftOtaContentToProcessShouldReturnNull() {
        val otaContent = OTAContent(
            project = contentConfig.project,
            version = 1,
            typeString = "draft",
            schema = 1,
        )
        otaRepository.getContentsValue = { listOf(otaContent) }

        runTest {
            assertThat(classUnderTest.otaContentToProcess()).isNull()
        }
    }

    @Test
    @DisplayName("Given draft OTA content, When otaContentToProcess is called with password, Then it should return content")
    fun draftOtaContentToProcessShouldReturnDraftContent() {
        draftContentManager.passcodeReturnValue = { "password" }
        val otaContent = OTAContent(
            project = contentConfig.project,
            version = 3,
            typeString = "draft",
            schema = 1,
        )
        contentManager.eligibleContentsToApplyValue = { setOf(draftContent) }
        otaRepository.getContentsValue = { listOf(otaContent) }

        runTest {
            assertThat(classUnderTest.otaContentToProcess()).isEqualTo(otaContent)
        }
    }
}
