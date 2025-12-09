package com.greencopper.thuzi.badges

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import com.greencopper.thuzi.badges.data.Badge
import com.greencopper.thuzi.badges.data.BadgesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BadgesViewModel(private val badgesRepository: BadgesRepository) : ViewModel() {
    suspend fun getBadges(badgesUrl: String): Flow<List<BadgeViewData>> =
        badgesRepository.getBadges(badgesUrl)
            .map { badges -> badges.map { badge -> badge.toViewData() } }

    private fun Badge.toViewData(): BadgeViewData {
        return BadgeViewData(
            id = badgeId,
            name = name,
            description = description,
            isEarned = this is Badge.EarnedBadge,
            image = badgesRepository.getImageDrawable(this)
        )
    }
}

internal data class BadgeViewData(
    val id: String,
    val name: String,
    val description: String,
    val isEarned: Boolean,
    val image: Drawable?,
)
