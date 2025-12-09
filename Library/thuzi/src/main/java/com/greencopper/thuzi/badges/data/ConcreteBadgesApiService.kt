package com.greencopper.thuzi.badges.data

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.thuzi.ThuziAPI
import com.greencopper.thuzi.ThuziResponse
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.App
import com.greencopper.toolkit.httpclient.saveToFile
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import kotlin.coroutines.CoroutineContext

internal class ConcreteBadgesApiService(
    private val thuziAPI: ThuziAPI,
    private val localStorage: LocalStorage,
    private val badgesImagesDirectory: String,
    private val backgroundContext: CoroutineContext,
) : BadgesApiService {

    @Throws
    override suspend fun getBadges(badgesUrl: String): List<Badge> = withContext(backgroundContext) {
        val id = localStorage.project.thuzi.qrCode.value ?:
            throw NoSuchElementException("Couldn't find value associated with key \"${localStorage.project.thuzi.qrCode.key}\"")
        val badgesAPIUrl = badgesUrl + id

        val response = thuziAPI.getBadges(badgesAPIUrl)

        return@withContext response.badges
            .filter { badge ->
                !badge.earnedImageUrl.isNullOrBlank() && !badge.unearnedImageUrl.isNullOrBlank()
            }
            .map { badgeApi -> badgeApi.toBadge() }
    }

    override suspend fun downloadImagesIfNeeded(badges: List<Badge>, shouldClean: Boolean): Flow<List<Badge>> =
        withContext(backgroundContext) {
            return@withContext flow {
                if (shouldClean) {
                    clean(badges)
                }
                missingImages(badges).forEach {
                    downloadImage(it)
                    emit(badges)
                }
            }
        }

    private suspend fun downloadImage(url: String): File = withContext(backgroundContext) {
        return@withContext thuziAPI.getBadgeImage(url).saveToFile(url, File(badgesImagesDirectory))
    }

    private fun missingImages(badges: List<Badge>): List<String> {
        val availableImages = File(badgesImagesDirectory).list() ?: arrayOf()

        return getUsefulImageUrls(badges).filter {
            !availableImages.contains(it.lastPathComponent())
        }
    }

    private fun uselessImages(badges: List<Badge>): List<String> {
        val availableImages = File(badgesImagesDirectory).list() ?: return emptyList()

        val usefulImageNames = getUsefulImageUrls(badges).map { it.lastPathComponent() }
        return availableImages.filter {
            !usefulImageNames.contains(it)
        }
    }

    private fun getUsefulImageUrls(badges: List<Badge>): List<String> =
        badges.flatMap {
            it.getUrls()
        }

    private fun clean(badges: List<Badge>) {
        val uselessImages = uselessImages(badges)
        uselessImages.forEach {
            try {
                File("$badgesImagesDirectory/$it").delete()
            } catch (t: Throwable) {
                App.log.e(message = "Badge's image deletion went wrong", throwable = t)
            }
        }
    }
}

internal fun ThuziResponse.Badge.toBadge() = if (isEarned) {
        Badge.EarnedBadge(
            badgeId,
            name ?: "",
            earnedDescription ?: "",
            earnedImageUrl!!,
            if (earnedOn != null)
                Instant.parse(earnedOn).toEpochMilli()
            else
                Instant.now().toEpochMilli()
        )
    } else {
        Badge.UnearnedBadge(
            badgeId,
            name ?: "",
            unearnedDescription ?: "",
            unearnedImageUrl!!,
            earnedDescription ?: "",
            earnedImageUrl!!
        )
    }
