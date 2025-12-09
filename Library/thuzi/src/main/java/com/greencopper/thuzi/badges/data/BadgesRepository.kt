package com.greencopper.thuzi.badges.data

import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow

internal interface BadgesRepository {

    suspend fun getBadges(badgesUrl: String): Flow<List<Badge>>

    fun getImageDrawable(badge: Badge): Drawable?
}
