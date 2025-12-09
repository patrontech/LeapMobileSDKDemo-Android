package com.greencopper.core.services.iplocation

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RestrictedAreaTest {
    private companion object {
        const val RESTRICTED_AREA_JSON = "[\"inRestrictedArea\",\"outsideRestrictedArea\"]"
    }

    init {
        Toolkit.setupTest()
    }

    @Test
    fun deserialization_succeeds() {
        val decoder: Json = App.resolve()
        val decoded: List<RestrictedArea> = decoder.decodeFromString(RESTRICTED_AREA_JSON)
        assertThat(decoded).isEqualTo(
            listOf(
                RestrictedArea.IN_RESTRICTED_AREA,
                RestrictedArea.OUTSIDE_RESTRICTED_AREA
            )
        )
    }

    @Test
    fun serialization_succeeds() {
        val encoder: Json = App.resolve()
        val encodable = listOf(
            RestrictedArea.IN_RESTRICTED_AREA,
            RestrictedArea.OUTSIDE_RESTRICTED_AREA
        )
        val encoded = encoder.encodeToString(encodable)
        assertThat(encoded).isEqualTo(RESTRICTED_AREA_JSON)
    }
}
