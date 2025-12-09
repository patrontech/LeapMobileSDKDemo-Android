package com.greencopper.thuzi.badges.data

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
public sealed class Badge : KiboSerializable<Badge> {
    public abstract val badgeId: String
    public abstract val name: String
    public abstract val description: String
    public abstract val imageUrl: String

    public open fun getUrls(): List<String> {
        return listOf(imageUrl)
    }

    override fun getSerializer(): KSerializer<Badge> = serializer()

    @Serializable
    public data class EarnedBadge(
        override val badgeId: String,
        override val name: String,
        override val description: String,
        override val imageUrl: String,
        val earnedDateInMillis: Long,
    ) : Badge()

    @Serializable
    public data class UnearnedBadge(
        override val badgeId: String,
        override val name: String,
        private val unearnedDescription: String,
        private val unearnedImageUrl: String,
        private val earnedDescription: String,
        private val earnedImageUrl: String,
    ) : Badge() {
        override val description: String = unearnedDescription
        override val imageUrl: String = unearnedImageUrl

        override fun getUrls(): List<String> {
            return listOf(unearnedImageUrl, earnedImageUrl)
        }

        public fun toEarnedBadge(earnedDate: Instant): EarnedBadge = EarnedBadge(
            badgeId = badgeId,
            name = name,
            description = earnedDescription,
            imageUrl = earnedImageUrl,
            earnedDateInMillis = earnedDate.toEpochMilli()
        )
    }
}

internal fun String.lastPathComponent(): String {
    return substringAfterLast("/")
}
