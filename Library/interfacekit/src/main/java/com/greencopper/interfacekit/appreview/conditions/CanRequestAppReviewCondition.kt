package com.greencopper.interfacekit.appreview.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.conditions.ConditionParameters
import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.appreview.localstorage.AppReviewRequest
import com.greencopper.interfacekit.appreview.localstorage.appReviewRequests
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Returns true if we can request an App Review
 */
internal class CanRequestAppReviewCondition(
    private val localStorage: LocalStorage,
    private val buildConfigProvider: BuildConfigProvider,
) : ParameterizedCondition<CanRequestAppReviewCondition.AppReviewRequestData>() {

    override fun checkWith(parameter: AppReviewRequestData): Boolean =
        checkParams(parameter, localStorage.app.interfaceKit.appReviewRequests.requests.value)

    override fun checkWithFlow(parameter: AppReviewRequestData): Flow<Boolean> =
        localStorage.app.interfaceKit.appReviewRequests.requests.state.map {
            checkParams(parameter, it)
        }

    override fun deserialize(conditionParameters: ConditionParameters): AppReviewRequestData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    private fun checkParams(parameter: AppReviewRequestData, reviewsRequested: List<AppReviewRequest>): Boolean {
        if (reviewsRequested.isEmpty()) return true

        val checkVersion = if (parameter.isNewVersion == true) {
            reviewsRequested.none { it.versionName == buildConfigProvider.versionName }
        } else true

        val checkInterval = parameter.intervalSincePreviousRequest?.let { threshold ->
            val lastRequest = reviewsRequested.maxBy { it.instant }.instant
            val intervalSinceLastRequest = Instant.now().epochSecond - lastRequest.epochSecond

            intervalSinceLastRequest >= threshold
        } ?: true

        return checkVersion && checkInterval
    }

    internal companion object {
        internal val key: ConditionInfo.Key = ConditionInfo.Key("InterfaceKit.CanRequestAppReview", 1)
    }

    @Serializable
    internal data class AppReviewRequestData(
        val isNewVersion: Boolean? = null,
        val intervalSincePreviousRequest: Long? = null,
    ) : KiboSerializable<AppReviewRequestData> {
        override fun getSerializer(): KSerializer<AppReviewRequestData> = serializer()
    }
}
