package com.greencopper.thuzi.badges.data

import kotlinx.coroutines.flow.Flow

internal interface BadgesApiService {
    suspend fun getBadges(badgesUrl: String): List<Badge>
    suspend fun downloadImagesIfNeeded(badges: List<Badge>, shouldClean: Boolean): Flow<List<Badge>>
}
