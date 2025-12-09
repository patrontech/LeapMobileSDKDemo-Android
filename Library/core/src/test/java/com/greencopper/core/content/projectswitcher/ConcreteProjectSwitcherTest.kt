package com.greencopper.core.content.projectswitcher

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.core.MockOTAManager
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class ConcreteProjectSwitcherTest {

    init {
        Toolkit.setupTest()
    }

    private val contentManager = MockContentManager()
    private val otaManager = MockOTAManager()

    private val classUnderTest = ConcreteProjectSwitcher(contentManager, otaManager)

    @Test
    @DisplayName("Given valid content and OTA are provided, When calling switchProject, Then valid content should be returned")
    fun switchProjectShouldSucceed() {
        val archive = ContentArchive(File(""), "secret")
        val content = Content(
            archive,
            1,
            1,
            "project",
            OTAContent.Type.Release,
        )
        val otaContent = OTAContent(
            "https://fakeUrl.fake",
            "default",
            "2020-03-20T10:20:24'Z'",
            1,
            "release",
            1
        )

        contentManager.currentContentValue = { content }
        contentManager.contentToApplyValue = { content }
        contentManager.applyValue = { _, _ -> content }

        otaManager.otaContentToProcessValue = { otaContent }
        otaManager.processValue = { content }

        runTest {
            assertThat(
                classUnderTest.switchProject(ProjectParams("default", otaContent.url!!))
            ).isEqualTo(content)
        }
    }

    @Test
    @DisplayName("Given projects are the same, When calling switchProject, Then null should be returned")
    fun switchProjectShouldReturnNull() {
        val archive = ContentArchive(File(""), "secret")
        val content = Content(
            archive,
            1,
            1,
            "project",
            OTAContent.Type.Release,
        )
        val otaContent = OTAContent(
            "https://fakeUrl.fake",
            "default",
            "2020-03-20T10:20:24'Z'",
            1,
            "release",
            1
        )

        contentManager.currentContentValue = { content }

        runTest {
            assertThat(
                classUnderTest.switchProject(ProjectParams("project", otaContent.url!!))
            ).isNull()
        }
    }

    @Test
    @DisplayName("Given no content to apply is found, When calling switchProject, Then IllegalStateException should be thrown")
    fun switchProjectShouldThrow() {
        val archive = ContentArchive(File(""), "secret")
        val content = Content(
            archive,
            1,
            1,
            "project",
            OTAContent.Type.Release,
        )
        val otaContent = OTAContent(
            "https://fakeUrl.fake",
            "default",
            "2020-03-20T10:20:24'Z'",
            1,
            "release",
            1
        )
        contentManager.contentToApplyValue = { null }
        contentManager.currentContentValue = { content }
        otaManager.otaContentToProcessValue = { otaContent }
        otaManager.processValue = { content }
        runTest {
            assertThrows<IllegalStateException> {
                classUnderTest.switchProject(ProjectParams("default", otaContent.url!!))
            }
        }
    }
}
