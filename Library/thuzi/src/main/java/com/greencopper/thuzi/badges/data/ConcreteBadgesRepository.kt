package com.greencopper.thuzi.badges.data

import android.graphics.drawable.Drawable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

internal class ConcreteBadgesRepository(
    private val badgesApiService: BadgesApiService,
    private val badgesCacheService: BadgesCacheService,
    private val coroutineContext: CoroutineContext,
    badgesImagesDirectory: String,
) : BadgesRepository {

    init {
        File(badgesImagesDirectory).mkdir()
    }

    override suspend fun getBadges(badgesUrl: String): Flow<List<Badge>> = withContext(coroutineContext) {
        flow {
            val cache = badgesCacheService.getBadges()
            try {
                if (cache.isNotEmpty()) {
                    emit(cache)
                    badgesApiService.downloadImagesIfNeeded(cache, false).collect {
                        emit(it)
                    }
                }
            } catch (t: Throwable) {
                App.log.e(
                    message = "Failed retrieving cached badges and downloading their images",
                    throwable = t
                )
                //continue and try to download up-to-date badges
            }

            try {
                val newBadges = badgesApiService.getBadges(badgesUrl)
                badgesCacheService.saveBadges(newBadges)
                emit(newBadges)
                badgesApiService.downloadImagesIfNeeded(newBadges, true).collect {
                    emit(it)
                }
            } catch (t: Throwable) {
                App.log.e(
                    message = "Failed retrieving new badges and downloading their images, may not throw",
                    throwable = t
                )
                if (cache.isEmpty()) {
                    throw t
                }
            }
        }
    }

    override fun getImageDrawable(badge: Badge): Drawable? {
        return badgesCacheService.getImageDrawable(badge)
    }
}
