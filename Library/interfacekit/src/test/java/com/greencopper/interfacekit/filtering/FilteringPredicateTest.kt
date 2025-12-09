package com.greencopper.interfacekit.filtering

import com.greencopper.interfacekit.filtering.FilteringPredicate.Logic
import com.greencopper.interfacekit.filtering.FilteringPredicate.Not
import com.greencopper.interfacekit.filtering.FilteringPredicate.Operator.AND
import com.greencopper.interfacekit.filtering.FilteringPredicate.Operator.OR
import com.greencopper.interfacekit.filtering.FilteringPredicate.Tag
import com.greencopper.parsimonious.ParseException
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FilteringPredicateTest {

    private var json: Json

    private val list1 = listOf("tag1")
    private val list2 = listOf("tag1", "tag2")
    private val list3 = listOf("tag3")
    private val list4 = listOf("tag1", "tag2", "tag3")
    private val list5 = listOf("tag1", "tag3")
    private val list6 = listOf("tag2")
    private val testList = listOf(list1, list2, list3, list4, list5, list6)

    init {
        Toolkit.setupTest()
        json = App.resolve()
    }

    //region Serialization
    @Test
    fun malformedPredicate_shouldFailOnDeserialize() {
        //then
        assertThrows<ParseException> {
            buildPredicate("TAGX filterId")
        }
    }

    @Test
    fun serializedTag_shouldDeserialize() {
        //given
        val originalTag = Tag("filterId")

        //when
        val tag = buildPredicate("$originalTag")

        //then
        assertThat(originalTag.toString()).isEqualTo(tag.toString())
    }

    @Test
    fun serializedFilter_shouldDeserialize() {
        //given
        val originalFilter = FilteringPredicate.Filter("filterId")

        //when
        val filter = buildPredicate("$originalFilter")

        //then
        assertThat(originalFilter.toString()).isEqualTo(filter.toString())
    }

    @Test
    fun serializedLogic_shouldDeserialize() {
        //given
        val originalTag = Tag("filterId")
        val originalFilter = FilteringPredicate.Filter("filterId")
        val originalLogic =
            Logic(originalTag, OR, originalFilter)

        //when
        val logic = buildPredicate("$originalLogic")

        //then
        assertThat(originalLogic.toString()).isEqualTo(logic.toString())
    }

    @Test
    fun serializedNotLogic_shouldDeserialize() {
        // given
        val originalTag = Tag("filterId")
        val originalFilter = FilteringPredicate.Filter("filterId")
        val originalLogic =
            Logic(originalTag, OR, originalFilter)
        val originalLogicNot = Not(originalLogic)

        // when
        val predicate = buildPredicate("$originalLogicNot")

        // then
        assertThat(originalLogicNot.toString()).isEqualTo(predicate.toString())
    }

    @Test
    fun serializeAndDeserialize() {
        val originalTag = Tag("tag1")
        val originalFilter = FilteringPredicate.Filter("filterId1")
        val originalLogic = Logic(originalTag, AND, originalFilter)

        val newTag = json.decodeFromString<Tag>(json.encodeToString(originalTag))
        assertThat(originalTag.toString()).isEqualTo(newTag.toString())
        val newFilter = json.decodeFromString<FilteringPredicate.Filter>(json.encodeToString(originalFilter))
        assertThat(originalFilter.toString()).isEqualTo(newFilter.toString())
        val newLogic = json.decodeFromString<Logic>(json.encodeToString(originalLogic))
        assertThat(originalLogic.toString()).isEqualTo(newLogic.toString())
    }
    //endregion

    //region Query
    @Test
    fun tag1_query_withoutArguments_shouldBeValid() {
        //given
        val predicate = buildPredicate("TAG tag1")

        //when
        val query = predicate.query()
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag1\"%'")
        assertThat(resultFilter).isEqualTo(listOf(list1, list2, list4, list5))
    }

    @Test
    fun tag1_query_withoutFilters_shouldBeValid() {
        //given
        val predicate = buildPredicate("TAG tag1")

        //when
        val query = predicate.query(emptyMap())
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag1\"%'")
        assertThat(resultFilter).isEqualTo(listOf(list1, list2, list4, list5))
    }

    @Test
    fun tag1_query_withFilters_shouldBeValid() {
        //given
        val predicate = buildPredicate("TAG tag1")
        val filters: Filters = mapOf(
            "filter1" to FilteringState.Filter.CheckBox(
                "label1",
                OR,
                emptyList()
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag1\"%'")
    }

    @Test
    fun tag1AndTag2_query_shouldBeValid() {
        //given
        val predicate = buildPredicate("TAG tag1 AND TAG tag2")

        //when
        val query = predicate.query(emptyMap())
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }

        //then
        assertThat(query?.toSQL()).isEqualTo("(tags LIKE '%\"tag1\"%' AND tags LIKE '%\"tag2\"%')")
        assertThat(resultFilter).isEqualTo(listOf(list2, list4))
    }

    @Test
    fun tag1OrTag2_query_withFilters_shouldBeValid() {
        //given
        val tag1 = "tag1"
        val tag2 = "tag2"
        val predicate = buildPredicate("TAG $tag1 OR TAG $tag2")

        //when
        val query = predicate.query(emptyMap())
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }

        //then
        assertThat(query?.toSQL()).isEqualTo("(tags LIKE '%\"$tag1\"%' OR tags LIKE '%\"$tag2\"%')")
        assertThat(resultFilter).isEqualTo(listOf(list1, list2, list4, list5, list6))
    }

    @Test
    fun tag1AndTag2OrTag3_query_withFilters_shouldBeValid() {
        //given
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"
        val predicate = buildPredicate("TAG $tag1 AND TAG $tag2 OR TAG $tag3")

        //when
        val query = predicate.query(emptyMap())
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }


        //then
        assertThat(query?.toSQL()).isEqualTo(
            "((tags LIKE '%\"$tag1\"%' " +
                    "AND tags LIKE '%\"$tag2\"%') " +
                    "OR tags LIKE '%\"$tag3\"%')"
        )
        assertThat(resultFilter).isEqualTo(listOf(list2, list3, list4, list5))
    }

    @Test
    fun notTag1AndTag2OrTag3_query_withFilters_shouldBeValid() {
        //given
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"
        val predicate = buildPredicate("NOT (TAG $tag1 AND TAG $tag2 OR TAG $tag3)")

        //when
        val query = predicate.query(emptyMap())
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }


        //then
        assertThat(query?.toSQL()).isEqualTo(
            "NOT (((tags LIKE '%\"$tag1\"%' " +
                    "AND tags LIKE '%\"$tag2\"%') " +
                    "OR tags LIKE '%\"$tag3\"%'))"
        )
        assertThat(resultFilter).isEqualTo(listOf(list1, list6))
    }

    @Test
    fun tag1AndTag2OrTag3_query_withParentheses_withFilters_shouldBeValid() {
        //given
        val tag1 = "tag1"
        val tag2 = "tag2"
        val tag3 = "tag3"
        val predicate = buildPredicate("TAG $tag1 AND (TAG $tag2 OR TAG $tag3)")

        //when
        val query = predicate.query(emptyMap())
        val resultFilter = testList.filter { query?.toPredicate()?.test(it) ?: true }

        //then
        assertThat(query?.toSQL()).isEqualTo(
            "(tags LIKE '%\"$tag1\"%' " +
                    "AND (tags LIKE '%\"$tag2\"%' " +
                    "OR tags LIKE '%\"$tag3\"%'))"
        )
        assertThat(query?.toString()).isEqualTo(
            "(TAG \"$tag1\" AND (TAG \"$tag2\" OR TAG \"$tag3\"))"
        )
        assertThat(resultFilter).isEqualTo(listOf(list2, list4, list5))
    }

    @Test
    fun filter1_query_withoutFilters_shouldNull() {
        //given
        val predicate = buildPredicate("FILTER filter1")

        //when
        val query = predicate.query(emptyMap())

        //then
        assertThat(query).isNull()
    }

    @Test
    fun filter2_query_withActiveFilters_shouldBeValid() {
        //given
        val predicate = buildPredicate("FILTER filter2")
        val filters: Filters = mapOf(
            "filter2" to FilteringState.Filter.CheckBox(
                "label2", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel21",
                        Tag("tag21"),
                        true
                    )
                )
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag21\"%'")
    }

    @Test
    fun filter3_query_withoutActiveFilters_shouldBeNull() {
        //given
        val predicate = buildPredicate("FILTER filter3")
        val filters: Filters = mapOf(
            "filter3" to FilteringState.Filter.CheckBox(
                "label3", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel31",
                        Tag("tag31"),
                        false
                    )
                )
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query).isNull()
    }

    @Test
    fun filter4_query_withActiveFilters_pointToOtherFilter_shouldBeValid() {
        //given
        val predicate = buildPredicate("FILTER filter4")
        val filters: Filters = mapOf(
            "filter2" to FilteringState.Filter.CheckBox(
                "label2", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel21",
                        Tag("tag21"),
                        true
                    )
                )
            ),
            "filter4" to FilteringState.Filter.CheckBox(
                "label4", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel41",
                        FilteringPredicate.Filter("filter2"),
                        true
                    )
                )
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag21\"%'")
    }

    @Test
    fun filter2AndFilter5_query_withActiveFilters_withOnlyFilter2_shouldBeValid() {
        //given
        val predicate = buildPredicate("FILTER filter2 AND FILTER filter5")
        val filters: Filters = mapOf(
            "filter2" to FilteringState.Filter.CheckBox(
                "label2", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel21",
                        Tag("tag21"),
                        true
                    )
                )
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag21\"%'")
    }

    @Test
    fun filter2AndFilter5_query_withActiveFilters_shouldBeValid() {
        //given
        val predicate = buildPredicate("FILTER filter2 AND FILTER filter5")
        val filters: Filters = mapOf(
            "filter2" to FilteringState.Filter.CheckBox(
                "label2", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel21",
                        Tag("tag21"),
                        true
                    )
                )
            ),
            "filter5" to FilteringState.Filter.CheckBox(
                "label5", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel51",
                        Tag("tag51"),
                        true
                    ),
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel52",
                        Tag("tag52"),
                        true
                    )
                )
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query?.toSQL()).isEqualTo("(tags LIKE '%\"tag21\"%' AND (tags LIKE '%\"tag51\"%' OR tags LIKE '%\"tag52\"%'))")
        assertThat(query?.toString()).isEqualTo("(TAG \"tag21\" AND (TAG \"tag51\" OR TAG \"tag52\"))")
    }

    @Test
    fun filter2AndFilter5_query_withActiveFilters_withOnlyFilter5_shouldBeValid() {
        //given
        val predicate = buildPredicate("FILTER filter2 AND FILTER filter5")
        val filters: Filters = mapOf(
            "filter5" to FilteringState.Filter.CheckBox(
                "label5", OR, listOf(
                    FilteringState.Filter.CheckBox.Option(
                        "optionLabel51",
                        Tag("tag51"),
                        true
                    )
                )
            )
        )

        //when
        val query = predicate.query(filters)

        //then
        assertThat(query?.toSQL()).isEqualTo("tags LIKE '%\"tag51\"%'")
    }
    //endregion

    //region Equals
    @Test
    fun tags_equals_check() {
        val tag1 = Tag("tag1")
        val tag1bis = Tag("tag1")
        val tag2 = Tag("tag2")
        val filter = FilteringPredicate.Filter("tag1")

        assertThat(tag1).isEqualTo(tag1bis)
        assertThat(tag1).isNotEqualTo(tag2)
        assertThat(tag1.hashCode()).isNotEqualTo(tag2.hashCode())
        assertThat(tag1.hashCode()).isEqualTo(tag1bis.hashCode())
        assertThat(tag1).isNotEqualTo(filter)
    }

    @Test
    fun filters_equals_check() {
        val filter1 = FilteringPredicate.Filter("filter1")
        val filter1bis = FilteringPredicate.Filter("filter1")
        val filter2 = FilteringPredicate.Filter("filter2")
        val tag = Tag("filter1")

        assertThat(filter1).isEqualTo(filter1bis)
        assertThat(filter1).isNotEqualTo(filter2)
        assertThat(filter1.hashCode()).isNotEqualTo(filter2.hashCode())
        assertThat(filter1.hashCode()).isEqualTo(filter1bis.hashCode())
        assertThat(filter1).isNotEqualTo(tag)
    }

    @Test
    fun logics_equals_check() {
        val tag1 = Tag("tag1")
        val tag2 = Tag("tag2")
        val tag3 = Tag("tag3")

        assertThat(Logic(tag1, OR, tag2)).isEqualTo(Logic(tag1, OR, tag2))
        assertThat(Logic(tag1, OR, tag2).hashCode()).isEqualTo(Logic(tag1, OR, tag2).hashCode())
        assertThat(Logic(tag1, OR, tag2)).isNotEqualTo(Logic(tag3, OR, tag2))
        assertThat(Logic(tag1, OR, tag2)).isNotEqualTo(Logic(tag1, AND, tag2))
        assertThat(Logic(tag1, OR, tag2)).isNotEqualTo(Logic(tag1, AND, tag3))
        assertThat(Logic(tag1, OR, tag2)).isNotEqualTo(tag1)
    }
    //endregion

    //region foldOr
    @Test
    fun foldOr_emptyList_shouldReturnNull() {
        //given
        val list = emptyList<String>()

        //when
        val result = list.foldOr()

        //then
        result shouldBe null
    }

    @Test
    fun foldOr_singleTag_shouldReturnPredicate() {
        //given
        val list = listOf("tag1")

        //when
        val result = list.foldOr()

        //then
        result shouldBe Tag("tag1")
    }

    @Test
    fun foldOr_multipleTags_shouldReturnPredicate() {
        //given
        val list = listOf("tag1", "tag2")

        //when
        val result = list.foldOr()

        //then
        result shouldBe Logic(Tag("tag1"), OR, Tag("tag2"))
    }
    //endregion

    private fun buildPredicate(pattern: String): FilteringPredicate =
        json.decodeFromString("\"$pattern\"")
}
