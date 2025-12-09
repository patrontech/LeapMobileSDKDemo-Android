package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.ContentRecipeInfo
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.testmocks.core.MockDraftContentManager
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

internal class ConcreteContentSelectorTest {

    init {
        Toolkit.setupTest()
    }

    private val contentConfiguration = RunConfiguration.Content(
        fileName = "file",
        secret = "secret",
        schema = 1,
        version = 1,
        project = "project"
    )
    private val archive = ContentArchive(File(""), contentConfiguration.secret)
    private val content = Content(
        archive,
        contentConfiguration.version,
        contentConfiguration.schema,
        contentConfiguration.project,
        OTAContent.Type.Release,
    )
    private val draftContent = Content(
        archive,
        contentConfiguration.version,
        contentConfiguration.schema,
        contentConfiguration.project,
        OTAContent.Type.Draft,
    )
    private val draftContentManager = MockDraftContentManager()

    private val classUnderTest = ConcreteContentSelector(
        contentConfiguration.schema,
        draftContentManager
    )

    @Test
    @DisplayName("Given there is one content to clean, When contentsToClean is called, Then this content should be returned")
    fun contentsToCleanShouldReturnOneContent() {
        content.currentState = State.Applied(setOf())

        val result = classUnderTest.contentsToClean(
            setOf(content),
            setOf(),
            content.copy(version = 3),
            null,
            CoreConfiguration.ContentConfig(60, emptyList())
        )
        assertThat(result).contains(content)
    }

    @Test
    @DisplayName("Given there is no content to clean, When contentsToClean is called, Then an empty set should be returned")
    fun contentsToCleanShouldReturnEmptySet() {
        content.currentState = State.Cleaned()

        val result = classUnderTest.contentsToClean(
            setOf(content),
            setOf(),
            content,
            null,
            CoreConfiguration.ContentConfig(60, emptyList())
        )
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given there is a current content, When contentsToClean is called, Then an empty set should be returned")
    fun contentsToCleanWithCurrentContentShouldReturnEmptySet() {
        content.currentState = State.Applied(setOf())

        val result = classUnderTest.contentsToClean(
            setOf(content),
            setOf(),
            content,
            null,
            CoreConfiguration.ContentConfig(60, emptyList())
        )
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given content has a different schema, When contentsToClean is called, Then an empty set should be returned")
    fun contentsToCleanWithDifferentSchemaShouldReturnEmptySet() {
        val contentConfiguration = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 2,
            version = 1,
            project = "project"
        )
        val content = Content(
            archive,
            contentConfiguration.version,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        )
        content.currentState = State.Applied(setOf())

        val result = classUnderTest.contentsToClean(
            setOf(content),
            setOf(),
            content.copy(version = 2),
            null,
            CoreConfiguration.ContentConfig(60, emptyList())
        )
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given content has expired, When contentsToClean is called, Then a content should be returned")
    fun contentsToCleanWithExpirationShouldReturnContent() {
        content.currentState = State.Applied(setOf(), Instant.parse("2007-12-03T10:15:30.00Z"))

        val result =
            classUnderTest.contentsToClean(
                setOf(content),
                setOf(),
                content.copy(version = 2),
                null,
                CoreConfiguration.ContentConfig(60, emptyList())
            )
        assertThat(result).contains(content)
    }

    @Test
    @DisplayName("Given content apply has timed out, When contentsToClean is called, Then a content should be returned")
    fun contentsToCleanWithApplyExpirationShouldReturnContent() {
        content.currentState = State.Applying(Instant.now().minus(100, ChronoUnit.MINUTES))

        val result =
            classUnderTest.contentsToClean(
                setOf(content),
                setOf(),
                content.copy(version = 2),
                null,
                CoreConfiguration.ContentConfig(60, emptyList())
            )
        assertThat(result).contains(content)
    }

    @Test
    @DisplayName("Given deprecated project, When contentsToClean is called, Then a content should be returned")
    fun contentsToCleanWithDeprecatedProjectsShouldReturnContent() {
        content.currentState = State.Opened(setOf())

        val result =
            classUnderTest.contentsToClean(
                setOf(content),
                setOf(),
                content.copy(version = 2),
                null,
                CoreConfiguration.ContentConfig(60, listOf("project"))
            )
        assertThat(result).contains(content)
    }

    @Test
    @DisplayName("Given other deprecated project, When contentsToClean is called, Then no content should be returned")
    fun contentsToCleanWithOtherDeprecatedProjectsShouldReturnEmptySet() {
        content.currentState = State.Applied(setOf())

        val result = classUnderTest.contentsToClean(
            setOf(content),
            setOf(),
            content.copy(version = 2),
            null,
            CoreConfiguration.ContentConfig(60, listOf("project_1"))
        )
        assertThat(result.first().project).isNotEqualTo("project_1")
    }

    @Test
    @DisplayName("Given forced content, When contentsToClean is called, Then content should be returned")
    fun contentsToCleanWithForcedContentShouldReturnContent() {
        content.currentState = State.Applied(setOf())

        val result = classUnderTest.contentsToClean(
            setOf(content),
            setOf(),
            content.copy(version = 2),
            content.copy(version = 3),
            CoreConfiguration.ContentConfig(60, listOf("project"))
        )
        assertThat(result).contains(content)
    }

    @Test
    @DisplayName("Given draft content in draft mode, When contentsToClean is called, Then contents should not be returned")
    fun contentsToCleanInDraftShouldNotReturnContent() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        draftContent.currentState = State.Applied(setOf())

        val result = classUnderTest.contentsToClean(
            setOf(draftContent),
            setOf(),
            draftContent,
            null,
            CoreConfiguration.ContentConfig(60, emptyList()),
        )

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given draft content, When contentsToClean is called when not in drat mode, Then contents should be returned")
    fun contentsToCleanShouldNotReturnDraftContent() {
        draftContentManager.passcodeReturnValue = { null }
        draftContent.currentState = State.Applied(setOf())
        val result = classUnderTest.contentsToClean(
            setOf(draftContent),
            setOf(),
            draftContent,
            null,
            CoreConfiguration.ContentConfig(60, emptyList()),
        )

        assertThat(result).contains(draftContent)

    }

    @Test
    @DisplayName("Given processed content is found, When eligibleContentsToApply is called, Then a content should be returned")
    fun eligibleContentsToApplyShouldReturnContent() {
        content.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))
        content.currentState = State.Processed(setOf(ContentRecipeKey("name", 1)))

        val result =
            classUnderTest.eligibleContentsToApply(
                setOf(content),
                setOf(ContentRecipeKey("name", 1)),
                content.project
            )
        assertThat(result).contains(content)
    }

    @Test
    @DisplayName("Given no processed content is found, When eligibleContentsToApply is called, Then an empty set should be returned")
    fun eligibleContentsToApplyShouldReturnEmptySet() {
        content.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))

        val result =
            classUnderTest.eligibleContentsToApply(setOf(content), setOf(), content.project)
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given no processed content for this project is found, When eligibleContentsToApply is called, Then an empty set should be returned")
    fun eligibleContentsToApplyWithDifferentProjectShouldReturnEmptySet() {
        content.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))
        content.currentState = State.Processed(setOf(ContentRecipeKey("name", 1)))

        val result =
            classUnderTest.eligibleContentsToApply(setOf(content), setOf(), "another_project")
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given draft content, When eligibleContentsToApply called with no password, Then empty set should be returned")
    fun eligibleContentsToApplyDraftContentNoPassword() {
        draftContent.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))
        draftContent.currentState = State.Processed(setOf(ContentRecipeKey("name", 1)))
        draftContentManager.passcodeReturnValue = { null }

        val result = classUnderTest.eligibleContentsToApply(setOf(draftContent), setOf(ContentRecipeKey("name", 1)), draftContent.project)

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("Given draft content, When eligibleContentsToApply called with password, Then draft content should be returned")
    fun eligibleContentsToApplyDraftContentWithPassword() {
        draftContent.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))
        draftContent.currentState = State.Processed(setOf(ContentRecipeKey("name", 1)))
        draftContentManager.passcodeReturnValue = { "password" }

        val result = classUnderTest.eligibleContentsToApply(setOf(draftContent), setOf(ContentRecipeKey("name", 1)), draftContent.project)

        assertThat(result).contains(draftContent)
    }

    @Test
    @DisplayName("Given valid content for this project is found, When contentToApply is called, Then content should be returned")
    fun contentToApplyShouldReturnContent() {
        content.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))
        content.currentState = State.Processed(setOf(ContentRecipeKey("name", 1)))

        val result = classUnderTest.contentToApply(
            setOf(content),
            setOf(ContentRecipeKey("name", 1)),
            "project"
        )
        assertThat(result).isEqualTo(content)
    }

    @Test
    @DisplayName("Given content is current content, When contentToApply is called, Then null should be returned")
    fun contentToApplyShouldReturnNull() {
        content.currentState = State.Opened(setOf(ContentRecipeInfo("name", 1)))
        content.currentState = State.Processed(setOf(ContentRecipeKey("name", 1)))

        val result = classUnderTest.contentToApply(setOf(content), setOf(), "project", content)
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("Given three contents not cleaned with the same project, When calling activeProjects, Then one project is returned")
    fun activeProjectsShouldReturnOneProject() {
        val firstContent = Content(
            archive,
            1,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        ).apply { currentState = State.Opened(emptySet()) }
        val secondContent = Content(
            archive,
            2,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        ).apply { currentState = State.Opened(emptySet()) }
        val thirdContent = Content(
            archive,
            3,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        ).apply { currentState = State.Processed(emptySet()) }
        val contents = setOf(firstContent, secondContent, thirdContent)

        assertThat(classUnderTest.activeProjects(contents).size).isEqualTo(1)
    }

    @Test
    @DisplayName("Given three contents not cleaned with different projects, When calling activeProjects, Then three projects are returned")
    fun activeProjectsShouldReturnThreeProjects() {
        val firstContent = Content(
            archive,
            1,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        ).apply { currentState = State.Opened(emptySet()) }
        val secondContent = Content(
            archive,
            2,
            contentConfiguration.schema,
            "project_2",
            OTAContent.Type.Release,
        ).apply { currentState = State.Opened(emptySet()) }
        val thirdContent = Content(
            archive,
            3,
            contentConfiguration.schema,
            "project_3",
            OTAContent.Type.Release,
        ).apply { currentState = State.Processed(emptySet()) }
        val contents = setOf(firstContent, secondContent, thirdContent)

        assertThat(classUnderTest.activeProjects(contents).size).isEqualTo(3)
    }

    @Test
    @DisplayName("Given three contents with one cleaned and different projects, When calling activeProjects, Then two projects are returned")
    fun activeProjectsShouldReturnTwoElements() {
        val firstContent = Content(
            archive,
            1,
            contentConfiguration.schema,
            contentConfiguration.project,
            OTAContent.Type.Release,
        ).apply { currentState = State.Opened(emptySet()) }
        val secondContent = Content(
            archive,
            2,
            contentConfiguration.schema,
            "project_2",
            OTAContent.Type.Release,
        ).apply { currentState = State.Cleaned() }
        val thirdContent = Content(
            archive,
            3,
            contentConfiguration.schema,
            "project_3",
            OTAContent.Type.Release,
        ).apply { currentState = State.Processed(emptySet()) }
        val contents = setOf(firstContent, secondContent, thirdContent)

        assertThat(classUnderTest.activeProjects(contents).size).isEqualTo(2)
    }

    @Test
    @DisplayName("Check projects before returned correctly")
    fun projectsBeforeReturnedCorrectly() {
        content.currentState = State.Applied(setOf(), date = ZonedDateTime.now().minusDays(1).toInstant())

        val contentConfiguration2 = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project2"
        )
        val archive2 = ContentArchive(File(""), contentConfiguration.secret)
        val content2 = Content(
            archive2,
            contentConfiguration2.version,
            contentConfiguration2.schema,
            contentConfiguration2.project,
            OTAContent.Type.Release,
        )
        content2.currentState = State.Applied(setOf())

        val contentConfiguration3 = RunConfiguration.Content(
            fileName = "file",
            secret = "secret",
            schema = 1,
            version = 1,
            project = "project3"
        )
        val archive3 = ContentArchive(File(""), contentConfiguration3.secret)
        val content3 = Content(
            archive3,
            contentConfiguration3.version,
            contentConfiguration3.schema,
            contentConfiguration3.project,
            OTAContent.Type.Release,
        )
        content3.currentState = State.ApplyingFailed(ZonedDateTime.now().toInstant())

        val result =
            classUnderTest.projectsBefore("project2", setOf(content, content2, content3))
        assertThat(result).isEqualTo(setOf(content.project))
    }


}
