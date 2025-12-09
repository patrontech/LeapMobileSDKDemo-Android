package com.greencopper.thuzi.mocks

import com.greencopper.thuzi.badges.data.Badge
import com.greencopper.thuzi.badges.data.BadgesApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MockBadgesApiService(
    var getBadges: () -> List<Badge> = { emptyList() },
    var downloadImagesIfNeeded: () -> Flow<List<Badge>> = { flowOf(emptyList()) }
) : BadgesApiService {

    override suspend fun getBadges(badgesUrl: String): List<Badge> = getBadges()

    override suspend fun downloadImagesIfNeeded(badges: List<Badge>, shouldClean: Boolean): Flow<List<Badge>> =
        downloadImagesIfNeeded()
}
