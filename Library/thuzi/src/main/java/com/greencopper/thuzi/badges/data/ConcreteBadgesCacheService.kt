package com.greencopper.thuzi.badges.data

import android.graphics.drawable.Drawable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.localstorage.thuzi

internal class ConcreteBadgesCacheService(
    private val localStorage: LocalStorage,
    private val badgesImagesDirectory: String,
) : BadgesCacheService {

    override fun saveBadges(badges: List<Badge>) {
        localStorage.project.thuzi.badges.value = badges
    }

    override fun getBadges(): List<Badge> = localStorage.project.thuzi.badges.value

    override fun getImageDrawable(badge: Badge): Drawable? {
        return try {
            Drawable.createFromPath("$badgesImagesDirectory/${badge.imageUrl.lastPathComponent()}")
        } catch (t: Throwable) {
            null
        }
    }
}