package com.greencopper.thuzi.mocks

import android.graphics.drawable.Drawable
import com.greencopper.thuzi.badges.data.Badge
import com.greencopper.thuzi.badges.data.BadgesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MockBadgesRepository(
    var badges: List<Badge> = emptyList(),
    var drawable: Drawable? = null,
) : BadgesRepository {

    override suspend fun getBadges(badgesUrl: String): Flow<List<Badge>> = flowOf(badges)

    override fun getImageDrawable(badge: Badge): Drawable? = drawable
}