package com.greencopper.thuzi.badges.data

import android.graphics.drawable.Drawable

internal interface BadgesCacheService {

    fun saveBadges(badges: List<Badge>)

    fun getBadges(): List<Badge>

    fun getImageDrawable(badge: Badge): Drawable?
}