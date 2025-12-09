package com.greencopper.core.content.serializers

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class InstantSerializerTest {

    @Test
    fun test_serialization() {
        //given
        Toolkit.setupTest()
        val json: Json = App.resolve()
        val date = Instant.now()

        //when
        val string = json.encodeToString(InstantSerializer, date)
        val decodedDate = json.decodeFromString(InstantSerializer, string)

        //then
        assertThat(InstantSerializer.descriptor.serialName).isEqualTo("InstantSerializer")
        assertThat(date.toEpochMilli()).isEqualTo(decodedDate.toEpochMilli())
    }
}