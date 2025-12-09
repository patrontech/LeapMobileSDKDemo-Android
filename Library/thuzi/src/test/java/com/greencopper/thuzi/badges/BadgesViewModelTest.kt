package com.greencopper.thuzi.badges

import com.greencopper.thuzi.badges.data.Badge
import com.greencopper.thuzi.mocks.MockBadgesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BadgesViewModelTest {

    private val mockBadges = listOf(
        Badge.EarnedBadge("1", "", "", "", 1L),
        Badge.UnearnedBadge("2", "", "", "", "", "")
    )

    private val viewModel = BadgesViewModel(
        MockBadgesRepository(badges = mockBadges)
    )

    @Test
    fun getBadges_returnsSameNumber() {
        runTest {
            val badges = viewModel.getBadges("").first()
            assertThat(badges.size).isEqualTo(mockBadges.size)
            assertThat(badges.filter { it.id == "1" }.size).isEqualTo(1)
            assertThat(badges.filter { it.id == "2" }.size).isEqualTo(1)
        }
    }
}