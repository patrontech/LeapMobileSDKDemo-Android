package com.greencopper.thuzi.badges.data

import android.graphics.drawable.Drawable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ConcreteBadgesCacheServiceTest {

    private val service: ConcreteBadgesCacheService

    private val badges = listOf(
        Badge.EarnedBadge(
            "1", "name", "description", "url", 1
        ),
        Badge.UnearnedBadge(
            "2","name",
            "unearnedDescription", "unearnedUrl",
            "earnedDescription", "earnedUrl",
        )
    )

    init {
        Toolkit.setupTest()
        service = ConcreteBadgesCacheService(App.resolve(), "badges")
        val localStorage = App.resolve<LocalStorage>()
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(1).toString()
    }

    @Test
    fun givenNoData_getBadges_returnsEmptyList() {
        assertThat(service.getBadges()).isEmpty()
    }

    @Test
    fun givenSaveBadges_getBadges_returnsBadges() {
        service.saveBadges(badges)

        assertThat(service.getBadges()).isEqualTo(badges)
    }

    @Test
    fun givenNoErrors_getDrawable_returnsDrawable() {
        mockkStatic(Drawable::class)
        every { Drawable.createFromPath(any()) } returns mockk()

        assertThat(service.getImageDrawable(badges[0])).isNotNull
    }

    @Test
    fun givenException_getDrawable_returnsNull() {
        mockkStatic(Drawable::class)
        every { Drawable.createFromPath(any()) } throws Exception()

        assertThat(service.getImageDrawable(badges[0])).isNull()
    }
}