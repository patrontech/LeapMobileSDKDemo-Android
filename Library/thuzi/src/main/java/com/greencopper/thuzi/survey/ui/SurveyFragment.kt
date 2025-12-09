package com.greencopper.thuzi.survey.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemId
import com.greencopper.core.services.track
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.viewModel
import com.greencopper.interfacekit.webview.ui.BaseWebViewFragment
import com.greencopper.thuzi.account.registration.PTWebViewInterface
import com.greencopper.thuzi.account.registration.model.RegistrationResponse
import com.greencopper.thuzi.survey.SurveyViewModel
import com.greencopper.thuzi.survey.SurveyWebViewLayoutData
import com.greencopper.toolkit.App

internal class SurveyFragment : BaseWebViewFragment<SurveyWebViewLayoutData>, RedirectableLayout {

    constructor(params: SurveyWebViewLayoutData) : super(params)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash


    private val viewModel: SurveyViewModel by viewModel()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.webview.addJavascriptInterface(
            PTWebViewInterface { response ->
                when (response.type) {
                    RegistrationResponse.ACTIVATION_COMPLETE, RegistrationResponse.DEVICE_LINKING_COMPLETE -> {
                        activity?.runOnUiThread {
                            activity?.onBackPressedDispatcher?.onBackPressed()
                        }
                    }
                }
            },
            JS_INTERFACE_NAME
        )

        if (lastWebviewState == null) {
            viewModel.injectCookie(data.url) {
                binding.webview.loadUrl(data.url)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.survey, mapOf(EventParameter.itemId to data.analytics.itemId)))
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateUserProfile()
    }

    private val Screen.Companion.survey: Screen
        get() = Screen(data.analytics.screenName, "thuzi_survey")

    override fun restoreData(encodedData: String): SurveyWebViewLayoutData = KiboSerializable.decodeFromString(encodedData)

    companion object {
        private const val JS_INTERFACE_NAME = "tzMobileMessageBroker"
    }

}
