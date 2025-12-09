package com.greencopper.interfacekit.search.logic

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SearchEntryTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun testSerializable() {
        val entry = SearchEntry(listOf("test1", "test2"),
            SearchEntry.ViewData.TitleSubtitle(
                title = "title",
                subtitle = "subtitle",
                image = null,
                routeLink = "routeLink"
            )
        )
        testKiboSerializable(entry)
    }

    @Test
    fun testEquals() {
        val entry1 = SearchEntry(listOf("test1", "test2"),
            SearchEntry.ViewData.TitleSubtitle(
                title = "title",
                subtitle = "subtitle",
                image = null,
                routeLink = "routeLink"
            )
        )

        val entry2 = SearchEntry(listOf("test1", "test2"),
            SearchEntry.ViewData.TitleSubtitle(
                title = "title",
                subtitle = "subtitle",
                image = null,
                routeLink = "routeLink"
            )
        )

        assertThat(entry1 == entry2).isTrue
    }

    @Test
    fun check_sortingValues() {
        val data = SearchEntry.ViewData.TitleSubtitle(
            title = "title",
            subtitle = "subtitle",
            image = null,
            routeLink = "routeLink"
        )

        assertThat(data.sortingValue).isEqualTo(data.title)
    }
}
