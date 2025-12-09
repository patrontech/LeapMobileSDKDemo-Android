package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.TestContentRecipe
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockProjectCleaner
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class ConcreteContentManagerTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private lateinit var classUnderTest: ConcreteContentManager

    private val coreConfigurationHolder = CoreConfigurationHolder()

    override fun afterEach() {}

    @Nested
    @DisplayName("Given content is valid and processor succeeds")
    inner class ProcessorSucceeds {
        private val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project"
        )
        private val archive = ContentArchive(File(""), contentConfiguration.secret)
        private val contentProcessor = ValidTestContentProcessor()
        private val contentSelector = ValidContentSelector(
            projectsBeforeValue = setOf("test")
        )
        private val contents = mutableSetOf<Content>()
        private val contentHistory = ValidContentHistory(contents = contents)
        private val projectCleaner = MockProjectCleaner()

        @BeforeEach
        fun setUp() {
            contentProcessor.reset()
            classUnderTest = ConcreteContentManager(
                contentProcessor,
                contentSelector,
                contentHistory,
                coreConfigurationHolder,
                projectCleaner,
                MockLogging(),
            )
        }

        @Test
        @DisplayName("When register is called, Then recipe factories is not empty")
        fun registerShouldSucceed() {
            assertDoesNotThrow {
                classUnderTest.register(mockk())
            }
            assertThat(classUnderTest.getRecipesForTest()).isNotEmpty
        }

        @Test
        @DisplayName("When process is called, Then contents is not empty and processor's open and process are called once")
        fun processShouldSucceed() {
            runTest {
                val content = Content(
                    archive,
                    contentConfiguration.version,
                    contentConfiguration.schema,
                    contentConfiguration.project,
                    OTAContent.Type.Release,
                )
                contents.add(content)
                classUnderTest.process(content)
                assertThat(classUnderTest.contents).isNotEmpty
                assertThat(contentProcessor.openCalled).isEqualTo(1)
                assertThat(contentProcessor.processCalled).isEqualTo(1)
            }
        }

        @Test
        @DisplayName("When apply is called, Then processor's apply is called once")
        fun applyShouldSucceed() = runTest {
            val isApplying = mutableListOf<Boolean>()

            val job = launch {
                classUnderTest.isApplyingContent.collect {
                    isApplying.add(it)
                }
            }
            delay(100)

            val content = Content(
                archive,
                contentConfiguration.version,
                contentConfiguration.schema,
                contentConfiguration.project,
                OTAContent.Type.Release,
            )
            contents.add(content)
            classUnderTest.register(TestContentRecipe())
            classUnderTest.apply(content)

            delay(100)
            contentProcessor.applyCalled shouldBe 1
            job.cancel()
            isApplying shouldBe listOf(false, true, false)
        }

        @Test
        @DisplayName("Projects before returned correctly")
        fun projectsBeforeReturnedCorrectly() {
            assertThat(classUnderTest.previousProjects).isEqualTo(setOf("test"))
        }
    }

    @Nested
    @DisplayName("Given processor is failing")
    inner class ProcessorFails {
        private val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project"
        )
        private val archive = ContentArchive(File(""), contentConfiguration.secret)
        private val contentProcessor = ErrorTestContentProcessor()
        private val contentSelector = ValidContentSelector()
        private val contentHistory = ValidContentHistory()
        private val projectCleaner = MockProjectCleaner()

        @BeforeEach
        fun setUp() {
            classUnderTest = ConcreteContentManager(
                contentProcessor,
                contentSelector,
                contentHistory,
                coreConfigurationHolder,
                projectCleaner,
                MockLogging(),
            )
        }

        @Test
        @DisplayName("When process is called, Then IllegalArgumentException is thrown")
        fun processShouldThrowIllegalArgumentException() {
            runTest {
                val content = Content(
                    archive,
                    contentConfiguration.version,
                    contentConfiguration.schema,
                    contentConfiguration.project,
                    OTAContent.Type.Release,
                )
                assertThrows<IllegalArgumentException> {
                    classUnderTest.process(content)
                }
            }
        }


        @Test
        @DisplayName("When apply is called, Then ProcessorApplyException is thrown")
        fun applyShouldThrowProcessorApplyException() = runTest {
            val isApplying = mutableListOf<Boolean>()

            val job = launch {
                classUnderTest.isApplyingContent.collect {
                    isApplying.add(it)
                }
            }
            delay(100)

            val content = Content(
                archive,
                contentConfiguration.version,
                contentConfiguration.schema,
                contentConfiguration.project,
                OTAContent.Type.Release,
            )
            assertThrows<NoSuchElementException> {
                classUnderTest.apply(content)
            }

            delay(100)
            job.cancel()
            isApplying shouldBe listOf(false, true, false)
        }
    }

    @Test
    @DisplayName("Given selector returns a content to apply for this project, When contentToApply is called, Then a content is returned")
    fun contentToApplyShouldSucceed() {
        val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project"
        )
        val archive = ContentArchive(File(""), contentConfiguration.secret)
        val contentProcessor = ValidTestContentProcessor()
        val content = Content(
            archive,
            contentConfiguration.version,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        )
        val contentSelector = ValidContentSelector(contentToApplyValue = content)
        val contentHistory = ValidContentHistory()
        val projectCleaner = MockProjectCleaner()


        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        assertThat(classUnderTest.contentToApply("project")).isEqualTo(content)
    }

    @Test
    @DisplayName("Given selector returns a content to apply for unknown project, When contentToApply is called, Then null is returned")
    fun contentToApplyShouldReturnNull() {
        val contentProcessor = ValidTestContentProcessor()
        val contentSelector = ValidContentSelector()
        val contentHistory = ValidContentHistory()
        val projectCleaner = MockProjectCleaner()

        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        assertThat(classUnderTest.contentToApply("unknown")).isNull()
    }

    @Test
    @DisplayName("Given selector returns an eligible content to apply for this project, When eligibleContentToApply is called, Then content is returned")
    fun eligibleContentToApplyShouldSucceed() {
        val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project",
        )
        val archive = ContentArchive(File(""), contentConfiguration.secret)
        val contentProcessor = ValidTestContentProcessor()
        val content = Content(
            archive,
            contentConfiguration.version,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        )
        val contentSelector = ValidContentSelector(eligibleContentsToApplyValue = setOf(content))
        val contentHistory = ValidContentHistory()
        val projectCleaner = MockProjectCleaner()


        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        assertThat(classUnderTest.eligibleContentsToApply("project")).isEqualTo(setOf(content))
    }

    @Test
    @DisplayName("Given selector returns an eligible content to apply for unknown project, When eligibleContentToApply is called, Then null is returned")
    fun eligibleContentToApplyShouldReturnNull() {
        val contentProcessor = ValidTestContentProcessor()
        val contentSelector = ValidContentSelector()
        val contentHistory = ValidContentHistory()
        val projectCleaner = MockProjectCleaner()


        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        assertThat(classUnderTest.eligibleContentsToApply("unknown")).isEmpty()
    }

    @Test
    @DisplayName("Given content to apply returns content and forced content is not null, When releaseForcedContent is called, Then content is returned")
    fun releaseForcedContentShouldSucceed() {
        val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project"
        )
        val archive = ContentArchive(File(""), contentConfiguration.secret)
        val contentProcessor = ValidTestContentProcessor()
        val content = Content(
            archive,
            contentConfiguration.version,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        )
        val contentSelector = ValidContentSelector(contentToApplyValue = content)
        val contentHistory = ValidContentHistory(
            currentProject = "project",
            forcedContent = content
        )
        val projectCleaner = MockProjectCleaner()

        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        runTest {
            val result = classUnderTest.releaseForcedContent()
            assertThat(result!!.first()).isEqualTo(content)
            assertThat(contentProcessor.cleanCalled).isEqualTo(1)
        }
    }

    @Test
    @DisplayName("Given content to apply returns content and forced content is null, When releaseForcedContent is called, Then null is returned")
    fun releaseForcedContentShouldReturnNull() {
        val contentProcessor = ValidTestContentProcessor()
        val contentSelector = ValidContentSelector()
        val contentHistory = ValidContentHistory()
        val projectCleaner = MockProjectCleaner()

        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        runTest {
            val result = classUnderTest.releaseForcedContent()
            assertThat(result).isNull()
        }
    }

    @Test
    @DisplayName("Given content to apply returns null, When releaseForcedContent is called, Then it should throw IllegalStateException")
    fun releaseForcedContentShouldThrow() {
        val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project"
        )
        val archive = ContentArchive(File(""), contentConfiguration.secret)
        val contentProcessor = ValidTestContentProcessor()
        val content = Content(
            archive,
            contentConfiguration.version,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        )
        val contentSelector = ValidContentSelector()
        val contentHistory = ValidContentHistory(
            currentProjectFlow = MutableSharedFlow<String?>(
                replay = 1
            ).apply { tryEmit("project") },
            forcedContent = content
        )
        val projectCleaner = MockProjectCleaner()

        classUnderTest = ConcreteContentManager(
            contentProcessor,
            contentSelector,
            contentHistory,
            coreConfigurationHolder,
            projectCleaner,
            MockLogging(),
        )

        runTest {
            assertThrows<IllegalStateException> {
                classUnderTest.releaseForcedContent()!!.first()
            }
        }
    }

    @Test
    @DisplayName("Given forced content is not null, When releaseForcedContentAtLaunch is called, Then content is returned")
    fun releaseForcedContentAtLaunchShouldSucceed() {
        runTest {

            val contentConfiguration = RunConfiguration.Content(
                fileName = "file",
                secret = "secret",
                schema = 1,
                version = 1,
                project = "project"
            )
            val archive = ContentArchive(File(""), contentConfiguration.secret)
            val contentProcessor = ValidTestContentProcessor()
            val content = Content(
                archive,
                contentConfiguration.version,
                contentConfiguration.schema,
                contentConfiguration.project,
                OTAContent.Type.Release,
            )
            val contentSelector = ValidContentSelector()
            val contentHistory = ValidContentHistory(
                currentProjectFlow = MutableSharedFlow<String?>(
                    replay = 1
                ).apply { tryEmit("project") },
                forcedContent = content
            )
            val projectCleaner = MockProjectCleaner()

            classUnderTest = ConcreteContentManager(
                contentProcessor,
                contentSelector,
                contentHistory,
                coreConfigurationHolder,
                projectCleaner,
                MockLogging(),
            )

            classUnderTest.releaseForcedContentAtLaunch()
            assertThat(contentProcessor.cleanCalled).isEqualTo(1)
        }
    }

    @Test
    @DisplayName("Given content to apply returns content and forced content is null, When releaseForcedContentAtLaunch is called, Then clean is not called")
    fun releaseForcedContentAtLaunchShouldReturn() {
        runTest {
            val contentProcessor = ValidTestContentProcessor()
            val contentSelector = ValidContentSelector()
            val contentHistory =
                ValidContentHistory(
                    currentProjectFlow = MutableSharedFlow<String?>(
                        replay = 1
                    ).apply {
                        tryEmit("project")
                    })
            val projectCleaner = MockProjectCleaner()

            classUnderTest = ConcreteContentManager(
                contentProcessor,
                contentSelector,
                contentHistory,
                coreConfigurationHolder,
                projectCleaner,
                MockLogging(),
            )

            classUnderTest.releaseForcedContentAtLaunch()
            assertThat(contentProcessor.cleanCalled).isEqualTo(0)
        }
    }

    @Test
    fun cleanObsoleteContentShouldReturn() {
        runTest {
            val contentProcessor = ValidTestContentProcessor()
            val contentSelector = ValidContentSelector()
            val contentHistory =
                ValidContentHistory(currentProjectFlow = MutableSharedFlow<String?>(replay = 1).apply {
                    tryEmit("project")
                })
            val projectCleaner = MockProjectCleaner()

            classUnderTest = ConcreteContentManager(
                contentProcessor,
                contentSelector,
                contentHistory,
                coreConfigurationHolder,
                projectCleaner,
                MockLogging(),
            )
            classUnderTest.cleanObsoleteContents()
            assertThat(contentProcessor.cleanCalled).isEqualTo(0)
        }
    }

    @Test
    fun cleanObsoleteContentShouldCallCleanOnce() {
        runTest {
            val contentProcessor = ValidTestContentProcessor()
            val element = Content(
                archive = ContentArchive(File(""), "secret"),
                version = 1,
                schema = 1,
                "project",
                OTAContent.Type.Release,
            )
            val contentSelector = ValidContentSelector(contentsToCleanValue = setOf(element))
            val contentHistory =
                ValidContentHistory(currentProjectFlow = MutableSharedFlow<String?>(replay = 1).apply {
                    tryEmit("project")
                })

            val coreConfiguration = CoreConfiguration(
                remoteState = CoreConfiguration.RemoteState("apiUrl", 20),
                notification = null,
                CoreConfiguration.OTA("apiUrl"),
                timezone = null,
                CoreConfiguration.ContentConfig(60, emptyList()),
                null
            )
            val projectCleaner = MockProjectCleaner()
            classUnderTest = ConcreteContentManager(
                contentProcessor,
                contentSelector,
                contentHistory,
                coreConfigurationHolder.apply { currentConfiguration.value = coreConfiguration },
                projectCleaner,
                MockLogging(),
            )
            classUnderTest.cleanObsoleteContents()
            assertThat(contentProcessor.cleanCalled).isEqualTo(1)
        }
    }
}
