package com.greencopper.core.content.serializers

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

internal class FileSerializerTest {

    @Test
    fun test_serialization() {
        //given
        Toolkit.setupTest()
        val json: Json = App.resolve()
        val file = File("/this/is/a/path")

        //when
        val string = json.encodeToString(FileSerializer, file)
        val decodedFile = json.decodeFromString(FileSerializer, string)

        //then
        Assertions.assertThat(FileSerializer.descriptor.serialName).isEqualTo("FileSerializer")
        Assertions.assertThat(file).isEqualTo(decodedFile)
    }
}