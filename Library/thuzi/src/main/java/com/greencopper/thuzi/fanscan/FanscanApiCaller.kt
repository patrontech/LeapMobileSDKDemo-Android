package com.greencopper.thuzi.fanscan

import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.services.track
import com.greencopper.thuzi.*
import com.greencopper.thuzi.metrics.fanScanCheckInFailure
import com.greencopper.thuzi.metrics.fanScanCheckInSuccess
import com.greencopper.toolkit.App

/**
 * A helper class to share code between [FanscanViewModel]
 * and [FanscanDeferredCommand]. No need to inject via DI.
 * Just create one as needed.
 */
internal class FanscanApiCaller(
    private val jwt: String,
    private val thuziAPI: ThuziAPI,
) {
    internal suspend fun checkIn(url: String, moduleId: String, deviceSession: String) =
        try {
            thuziAPI.checkIn("$url$moduleId/$deviceSession", "Bearer $jwt".takeIf { jwt.isNotEmpty() }.orEmpty())
            App.track(CheckInSuccessEvent(moduleId))
        } catch (e: Exception) {
            App.track(CheckInFailureEvent(moduleId))
            throw e
        }

    internal data class CheckInSuccessEvent(
        private val itemId: String
    ) : MappedMetrics {
        override fun track(provider: MappedProvider) {
            val eventName = EventName.fanScanCheckInSuccess
            val parameters = mapOf(EventParameter.itemId to itemId)
            provider.track(eventName, parameters)
        }
    }

    internal data class CheckInFailureEvent(
        private val itemId: String
    ) : MappedMetrics {
        override fun track(provider: MappedProvider) {
            val eventName = EventName.fanScanCheckInFailure
            val parameters = mapOf(EventParameter.itemId to itemId)
            provider.track(eventName, parameters)
        }
    }
}
