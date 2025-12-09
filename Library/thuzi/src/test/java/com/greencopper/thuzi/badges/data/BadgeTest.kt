package com.greencopper.thuzi.badges.data

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

internal class BadgeTest {

    @Test
    fun unearnedBadge_toEarnedBadge() {
        val unearnedBadge = Badge.UnearnedBadge(
            badgeId = "id",
            name = "name",
            unearnedDescription = "unearned description",
            unearnedImageUrl = "unearned url",
            earnedDescription = "earned description",
            earnedImageUrl = "earned url"
        )

        val earnedBadge = unearnedBadge.toEarnedBadge(Instant.parse("2007-12-03T10:15:30.00Z"))

        assertThat(unearnedBadge.badgeId).isEqualTo(earnedBadge.badgeId)
        assertThat(unearnedBadge.name).isEqualTo(earnedBadge.name)
    }
}