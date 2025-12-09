package com.greencopper.core.content.initialcontent

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockStorageManager
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class RunConfigurationTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun init() {
        val latestVersion = 90
        val latestSchema = 1
        val runConfiguration = RunConfiguration.build(App.resolve(), json)
        val contentConfiguration = RunConfiguration.Content(
            "content_v$latestVersion.zip",
            "a723380fb8c94a8c868111da255833f7",
            latestSchema,
            latestVersion,
            "kibo-2022"
        )
        val runConfiguration2 = RunConfiguration(contentConfiguration)
        assertThat(runConfiguration).isEqualTo(
            runConfiguration2
        )
    }

    @Test
    fun deserialization_success() {
        val contentConfiguration = RunConfiguration.Content(
            "content_v4.zip",
            "a723380fb8c94a8c868111da255833f7",
            1,
            4,
            "defaultTag",
            listOf("a1, b2"),
        )
        val runConfiguration = RunConfiguration(contentConfiguration)
        val stringConfiguration = json.encodeToString(RunConfiguration.serializer(), runConfiguration)
        val runConfiguration2 = json.decodeFromString(RunConfiguration.serializer(), stringConfiguration)
        assertThat(runConfiguration).isEqualTo(runConfiguration2)
    }

    @Test
    fun `build should throw Error when reading file fails`() {
        val storageManager = MockStorageManager(
            assetAsFile = { throw Exception("Read error") },
        )

        assertThrows<Exception> { RunConfiguration.build(storageManager, json) }
    }

    @Test
    fun deserializationError_shouldThrow() {
        val storageManager = MockStorageManager(
            assetAsFile = { mockk<File>() },
        )
        val json = mockk<Json>()
        every { json.decodeFromString<RunConfiguration>(any(), any()) } throws Exception("Deserialization error")

        assertThrows<Exception> { RunConfiguration.build(storageManager, json) }
    }

}
