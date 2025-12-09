package com.greencopper.interfacekit.filtering

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FilteringStateTest {

    @Test
    fun checkbox_predicate_shouldBeValid() {
        val checkBox = FilteringState.Filter.CheckBox(
            "label1", FilteringPredicate.Operator.OR, listOf(
                FilteringState.Filter.CheckBox.Option(
                    "option1",
                    FilteringPredicate.Tag("tag1"),
                    true
                ),
                FilteringState.Filter.CheckBox.Option(
                    "option2",
                    FilteringPredicate.Tag("tag2"),
                    false
                ),
                FilteringState.Filter.CheckBox.Option(
                    "option3",
                    FilteringPredicate.Filter("filter1"),
                    true
                ),
            )
        )

        assertThat(checkBox.predicate).isEqualTo(
            FilteringPredicate.Logic(
                FilteringPredicate.Tag("tag1"),
                FilteringPredicate.Operator.OR,
                FilteringPredicate.Filter("filter1")
            )
        )
    }

    @Test
    fun checkbox_option_test_equals() {
        val option1 =
            FilteringState.Filter.CheckBox.Option("label1", FilteringPredicate.Tag("tag1"), true)
        val option2 =
            FilteringState.Filter.CheckBox.Option("label1", FilteringPredicate.Tag("tag1"), true)
        val option3 =
            FilteringState.Filter.CheckBox.Option("label1", FilteringPredicate.Tag("tag3"), true)

        assertThat(option1.label).isEqualTo(option2.label)
        assertThat(option1.predicate).isEqualTo(option2.predicate)
        assertThat(option1.isActive).isEqualTo(option2.isActive)

        assertThat(option1).isEqualTo(option2)
        assertThat(option1).isNotEqualTo(option3)
    }

}