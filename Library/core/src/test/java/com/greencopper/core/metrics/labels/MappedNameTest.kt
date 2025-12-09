package com.greencopper.core.metrics.labels

import com.greencopper.core.metrics.provider.Provider
import com.greencopper.core.metrics.provider.default
import com.greencopper.core.metrics.provider.firebase
import com.greencopper.testmocks.core.testProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MappedNameTest {

    @Test
    fun whenSettingValueForSingleProvider_shouldOnlyApplyToProvider() {
        // given
        val event = EventName("screen_view")
        // when
        event[Provider.firebase] = "SCREEN_VIEW"
        // then
        assertThat(event).isNotNull
        assertThat(event[Provider.default]).isNotNull
        assertThat(event[Provider.firebase]).isNotNull
        assertThat(event[Provider.default]).isEqualTo("screen_view")
        assertThat(event[Provider.firebase]).isEqualTo("SCREEN_VIEW")
    }

    @Test
    fun whenIgnoreSingleProvider_shouldBeNull() {
        // given
        val event = EventName("screen_view")
        // when
        val value = event.ignore(setOf(Provider.firebase))
        assertThat(value).isEqualTo(Unit)
        // then
        assertThat(event).isNotNull
        assertThat(event[Provider.firebase]).isNull()
        assertThat(event[Provider.default]).isEqualTo("screen_view")
        assertThat(event[Provider.testProvider]).isEqualTo("screen_view")
    }

    @Test
    fun whenWithoutSingleProvider_shouldBeNull() {
        // given
        val event = EventName("screen_view")
        // when
        val returnedValue = event.without(setOf(Provider.firebase))
        assertThat(returnedValue).isNotNull
        // then
        assertThat(event).isNotNull
        assertThat(event[Provider.firebase]).isNull()
        assertThat(event[Provider.default]).isEqualTo("screen_view")
        assertThat(event[Provider.testProvider]).isEqualTo("screen_view")
    }

    @Test
    fun whenSettingValueForMultipleProviders_shouldApplyToThoseProviders() {
        // given
        val event = EventName("screen_view")
        // when
        event[setOf(Provider.firebase, Provider.testProvider)] = "SCREEN_VIEW"
        // // then
        assertThat(event[Provider.default]).isNotNull
        assertThat(event[Provider.firebase]).isNotNull
        assertThat(event[Provider.firebase]).isEqualTo("SCREEN_VIEW")
        assertThat(event[Provider.testProvider]).isEqualTo("SCREEN_VIEW")
        assertThat(event[Provider.default]).isEqualTo("screen_view")
    }

    @Test
    fun whenNoCustomValueSet_shouldDefaultToDefault() {
        // given event
        val event = EventName("screen_view")

        // firebase should default to default value if not present
        assertThat(event[Provider.default]).isEqualTo(event[Provider.firebase])
    }

    @Test
    fun whenResetting_customValuesAreRemoved() {
        val event = EventName("screen_view")
        event[Provider.firebase] = "SCREEN_VIEW"
        assertThat(event[Provider.firebase]).isEqualTo("SCREEN_VIEW")
        assertThat(event[Provider.default]).isEqualTo("screen_view")
        event.reset()
        assertThat(event[Provider.firebase]).isEqualTo("screen_view")
        assertThat(event[Provider.default]).isEqualTo("screen_view")
    }
}