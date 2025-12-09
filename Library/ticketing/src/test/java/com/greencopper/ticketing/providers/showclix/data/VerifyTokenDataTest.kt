package com.greencopper.ticketing.providers.showclix.data

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class VerifyTokenDataTest {
    init {
        Toolkit.setupTest()
    }

    private val originalData = VerifyTokenData(
        VerifyTokenData.Data(
            "id123",
            VerifyTokenData.Data.Attributes(
                "email",
                "token"
            )
        )
    )

    @Test
    fun serializeAndDeserialize() {
        assertDoesNotThrow {
            val data =
                KiboSerializable.decodeFromString<VerifyTokenData>(originalData.encodeToString())
            assertThat(data.data.id).isEqualTo(originalData.data.id)
            assertThat(data.data.attributes.email).isEqualTo(originalData.data.attributes.email)
            assertThat(data.data.attributes.validationToken).isEqualTo(originalData.data.attributes.validationToken)
        }
    }
}