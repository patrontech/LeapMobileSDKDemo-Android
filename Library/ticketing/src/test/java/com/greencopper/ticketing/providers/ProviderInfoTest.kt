package com.greencopper.ticketing.providers

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketingmocks.providers.MockProvider
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class ProviderInfoTest {
    init {
        Toolkit.setupTest()
    }

    @Test
    fun serializeAndDeserialize() {

        val originalData = MockProvider.Data("apiUrl")
        val originalInfo = ProviderInfo(
            ProviderInfo.Key(
                "id123",
                1
            ),
            originalData.encodeToJsonElement()
        )

        assertDoesNotThrow {
            val info =
                KiboSerializable.decodeFromString<ProviderInfo>(originalInfo.encodeToString())
            assertThat(info.key).isEqualTo(originalInfo.key)
            assertThat(KiboSerializable.decodeFromJsonElement<MockProvider.Data>(info.params!!).apiUrl)
                .isEqualTo(originalData.apiUrl)
        }
    }
}
