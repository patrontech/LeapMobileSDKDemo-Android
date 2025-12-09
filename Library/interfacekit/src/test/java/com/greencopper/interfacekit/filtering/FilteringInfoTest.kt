package com.greencopper.interfacekit.filtering

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FilteringInfoTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun serializeAndDeserialize() {
        //given
        val filteringInfo = FilteringInfo(FilteringPredicate.Tag("tag1"), mapOf(
            "filter1" to FilterInfo.CheckBox("checkBox1", FilteringPredicate.Operator.OR, listOf(
                FilterInfo.CheckBox.Option("option11", FilteringPredicate.Tag("option11tag"), true),
                FilterInfo.CheckBox.Option("option12", FilteringPredicate.Tag("option12tag")),
                FilterInfo.CheckBox.Option("option13", FilteringPredicate.Tag("option13tag"), true)
            ), 1)
        ))

        //when
        val newFilteringInfo = KiboSerializable.decodeFromString<FilteringInfo>(filteringInfo.encodeToString())

        //then
        assertThat(filteringInfo.predicate).isEqualTo(newFilteringInfo.predicate)
        assertThat(filteringInfo.filters).isEqualTo(newFilteringInfo.filters)
    }
}