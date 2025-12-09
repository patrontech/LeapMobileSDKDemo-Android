package com.greencopper.interfacekit.appreview.commands

import com.google.android.play.core.review.ReviewManager
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.appreview.localstorage.AppReviewRequest
import com.greencopper.interfacekit.appreview.localstorage.appReviewRequests
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.UnparameterizedCommand
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant

internal class RequestAppReviewCommand(
    private val localStorage: LocalStorage,
    private val buildConfigProvider: BuildConfigProvider,
    private val reviewManager: ReviewManager,
    private val scope: CoroutineScope,
) : UnparameterizedCommand() {

    override fun execute(origin: Layout?): Flow<Boolean> {
        val activity = origin?.activity ?: return flowOf(false)

        scope.launch {
            val request = reviewManager.requestReviewFlow()
            request.addOnCompleteListener {
                if (it.isSuccessful) {
                    reviewManager.launchReviewFlow(activity, it.result).addOnCompleteListener {
                        requestReviewCompleted()
                    }
                }
            }
        }

        return flowOf(true)
    }

    private fun requestReviewCompleted() {
        val requests = localStorage.app.interfaceKit.appReviewRequests.requests.value
        localStorage.app.interfaceKit.appReviewRequests.requests.value = requests + AppReviewRequest(
            buildConfigProvider.versionName,
            Instant.now()
        )
    }

    companion object {
        val key: CommandInfo.Key = CommandInfo.Key("InterfaceKit.RequestAppReview", 1)
    }
}
