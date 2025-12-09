package com.greencopper.interfacekit.appreview.localstorage

import com.greencopper.core.content.serializers.InstantSerializer
import com.greencopper.core.localstorage.*
import com.greencopper.interfacekit.common.InterfaceKitAppLocalStorageDomain
import kotlinx.serialization.Serializable
import java.time.Instant

internal class AppReviewRequestsLocalStorageDomain(parent: InterfaceKitAppLocalStorageDomain) :
    LocalStorageDomainBase("appReviewRequests", parent) {

    internal val requests: LocalStorageProperty<List<AppReviewRequest>> by localStorageProperty(
        emptyList()
    )
}

@Serializable
internal data class AppReviewRequest(
    val versionName: String,
    @Serializable(with = InstantSerializer::class)
    val instant: Instant,
)

internal val InterfaceKitAppLocalStorageDomain.appReviewRequests: AppReviewRequestsLocalStorageDomain
    get() = AppReviewRequestsLocalStorageDomain(this)
