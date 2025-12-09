package com.greencopper.core.content.initialcontent

import com.greencopper.core.content.manager.*
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.core.mock
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ConcreteContentInitializerTest {

    init {
        Toolkit.setupTest()
    }

    private val contentManager = MockContentManager(
        currentContentValue = { null },
        contentToApplyValue = { null },
        forcedContentValue = { null },
    )

    private val storageManager: StorageManager = App.resolve()
    private val contentConfiguration = RunConfiguration.Content(
        fileName = "fileName",
        secret = "secret",
        schema = 3,
        version = 3,
        project = "initial",
    )

    private val contentInitializer = ConcreteContentInitializer(
        manager = contentManager,
        runConfigContent = contentConfiguration,
        storageManager = storageManager,
        logging = MockLogging(),
    )

    @Test
    fun whenInitializing_withValidForcedContent_shouldApplyIt() {
        //given
        val forcedContent = Content.mock(1, 3, "forced")
        contentManager.forcedContentValue = { forcedContent }
        contentManager.applyValue = { contentApplied, _ ->
                contentApplied.currentState = State.Applied(emptySet())
                contentApplied
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(content).isEqualTo(forcedContent)
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withValidForcedContent_withValidContentToApply_shouldApplyForced() {
        //given
        val forcedContent = Content.mock(1, 3, "forced")
        contentManager.contentToApplyValue = { Content.mock(4, 3, "toApply") }
        contentManager.forcedContentValue = { forcedContent }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.currentState = State.Applied(emptySet())
            contentApplied
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(content).isEqualTo(forcedContent)
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withValidForcedContent_withFail_shouldApplyInitial() {
        //given
        var triedToApplyForced = false
        contentManager.forcedContentValue = { Content.mock(1, 3, "forced") }
        contentManager.processValue = { contentProcessed ->
            contentProcessed.apply {
                contentProcessed.currentState = State.Processed(emptySet())
            }
        }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                if (project == "forced") {
                    triedToApplyForced = true
                    throw NullPointerException()
                } else {
                    currentState = State.Applied(emptySet())
                }
            }
            contentApplied
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(triedToApplyForced).isTrue
            assertThat(content.project).isEqualTo("initial")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withInvalidForcedContent_shouldApplyInitial() {
        //given
        var triedToApplyForced = false

        contentManager.forcedContentValue = { Content.mock(1, 2, "forced") }
        contentManager.processValue = { contentProcessed ->
            contentProcessed.apply {
                contentProcessed.currentState = State.Processed(emptySet())
            }
        }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                if (project == "forced") {
                    triedToApplyForced = true
                    throw NullPointerException()
                } else {
                    currentState = State.Applied(emptySet())
                }
            }
            contentApplied
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(triedToApplyForced).isFalse
            assertThat(content.project).isEqualTo("initial")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withValidContentToApply_shouldApplyIt() {
        //given
        contentManager.contentToApplyValue = { Content.mock(4, 3, "initial") }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                currentState = State.Applied(emptySet())
            }
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(content.project).isEqualTo("initial")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withValidContentToApply_withFailApplying_shouldApplyInitial() {
        //given
        var triedToApplyContent = false
        contentManager.contentToApplyValue = { Content.mock(4, 3, "toApply") }
        contentManager.processValue = { contentProcessed ->
            contentProcessed.apply {
                contentProcessed.currentState = State.Processed(emptySet())
            }
        }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                if (project == "toApply") {
                    triedToApplyContent = true
                    throw NullPointerException()
                } else {
                    currentState = State.Applied(emptySet())
                }
            }
            contentApplied
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(triedToApplyContent).isTrue()
            assertThat(content.project).isEqualTo("initial")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withValidContentToApply_withWrongVersion_shouldApplyInitial() {
        //given
        var triedToApplyContent = false
        contentManager.contentToApplyValue = { Content.mock(2, 3, "toApply") }
        contentManager.processValue = { contentProcessed ->
            contentProcessed.apply {
                contentProcessed.currentState = State.Processed(emptySet())
            }
        }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                if (project == "toApply") {
                    triedToApplyContent = true
                    throw NullPointerException()
                } else {
                    currentState = State.Applied(emptySet())
                }
            }
            contentApplied
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(triedToApplyContent).isTrue
            assertThat(content.project).isEqualTo("initial")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withValidContentToApply_withDeprecatedProject_shouldApplyInitial() {
        //given
        contentManager.contentToApplyValue = { Content.mock(4, 3, "initial") }
        contentManager.processValue = { contentProcessed ->
            contentProcessed.apply {
                contentProcessed.currentState = State.Processed(emptySet())
            }
        }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                currentState = State.Applied(emptySet())
            }
        }
        val config = RunConfiguration.Content(
            fileName = "fileName",
            secret = "secret",
            schema = 3,
            version = 3,
            project = "newProject",
            deprecatedProjects = listOf("initial")
        )

        val contentInitializer = ConcreteContentInitializer(
            manager = contentManager,
            runConfigContent = config,
            storageManager = storageManager,
            logging = MockLogging(),
        )

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(content.project).isEqualTo("newProject")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withoutContent_shouldApplyInitial() {
        //given
        contentManager.processValue = { contentProcessed ->
            contentProcessed.apply {
                contentProcessed.currentState = State.Processed(emptySet())
            }
        }
        contentManager.applyValue = { contentApplied, _ ->
            contentApplied.apply {
                currentState = State.Applied(emptySet())
            }
        }

        runTest {
            //when
            val content = contentInitializer.initialize()

            //then
            assertThat(content.project).isEqualTo("initial")
            assertThat(content.currentState).isInstanceOf(State.Applied::class.java)
        }
    }

    @Test
    fun whenInitializing_withAlreadyAppliedContent_shouldThrow() {
        contentManager.currentContentValue = { Content.mock(4, 3, "toApply") }

        val exception = assertThrows<ContentException.InitializerProcessException> {
            runTest { contentInitializer.initialize() }
        }
        assertThat(exception.cause).isInstanceOf(IllegalStateException::class.java)
    }
}
