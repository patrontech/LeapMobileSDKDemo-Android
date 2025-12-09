package com.greencopper.core.content.serializers

import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ZonedDateTimeWithInstantSerializerTest {

    @Test
    fun test_serialization() {
        Toolkit.setupTest()
        bindSingleton<TimezoneProvider>(MockTimezoneProvider())
        val json: Json = App.resolve()

        //given
        val date = ZonedDateTime.now().withNano(0)

        //when
        val string = json.encodeToString(ZonedDateTimeWithInstantSerializer, date)
        val decodedDate = json.decodeFromString(ZonedDateTimeWithInstantSerializer, string)

        //then
        assertThat(ZonedDateTimeWithInstantSerializer.descriptor.serialName).isEqualTo("ZonedDateTimeWithInstantSerializer")
        assertThat(date).isEqualTo(decodedDate)
    }
}
