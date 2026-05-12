package com.example.kibasdkpoc.deeplink

import com.example.kibasdkpoc.R

public const val DEEPLINK_SCHEME: String = "leapfanfest"

public val deeplinkUris: List<Pair<String, Int>> = listOf(
	Pair("$DEEPLINK_SCHEME://scheduleList", R.string.schedule),
	Pair("$DEEPLINK_SCHEME://talents", R.string.talents),
	Pair("$DEEPLINK_SCHEME://brands", R.string.brands),
	Pair("$DEEPLINK_SCHEME://notificationSettings", R.string.notification_settings),
	Pair("$DEEPLINK_SCHEME://thuziRegistration", R.string.registration),
	Pair("$DEEPLINK_SCHEME://thuziBadges", R.string.badges),
	Pair("$DEEPLINK_SCHEME://invalid", R.string.invalid),
)
