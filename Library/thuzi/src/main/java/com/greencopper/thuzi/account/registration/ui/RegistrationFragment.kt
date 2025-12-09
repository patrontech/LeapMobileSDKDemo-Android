package com.greencopper.thuzi.account.registration.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.viewModel
import com.greencopper.interfacekit.webview.ui.BaseWebViewFragment
import com.greencopper.thuzi.account.registration.PTWebViewInterface
import com.greencopper.thuzi.account.registration.RegistrationViewModel
import com.greencopper.thuzi.account.registration.model.*
import com.greencopper.thuzi.metrics.thuziRegistration
import com.greencopper.thuzi.style.ThuziColor
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException

internal class RegistrationFragment : BaseWebViewFragment<RegistrationLayoutData>,
    RedirectableLayout, OnboardingPageLayout {

    constructor(registrationWebData: RegistrationLayoutData) : super(registrationWebData)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    companion object {
        private const val JS_INTERFACE_NAME = "tzMobileMessageBroker"
    }

    private var activationComplete: Boolean = false
    private var authenticated: Boolean = false
    private var registrationComplete: Boolean = false

    /**
     * This exists for backwards compatibility and can
     * probably be removed at some point, though it's
     * harmless to leave it in.
     *
     * Thuzi has added an `attendeeHasAuthenticated`
     * flag to the payload of the `ACTIVATION_COMPLETE`
     * event. This tells us whether we can expect an
     * `ATTENDEE_AUTHENTICATED` event or not.
     *
     * But what if we don't have this flag or this
     * payload? In that case, we don't know whether
     * we're going to get `ATTENDEE_AUTHENTICATED`,
     * so we wait 1 full second to get it. After that,
     * if we don't get it, we simulate it by setting
     * the `authenticated` flag to `true`.
     *
     * This will probably never happen in the wild,
     * but if it does, the user will be stuck without
     * this.
     */
    private var delayedAuthenticationJob: Job? = null

    override var loadUrl = false

    override val screenColor: ScreenColor get() = ThuziColor.registration

    private val viewModel: RegistrationViewModel by viewModel()

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            startRegistration()
        }
        binding.webview.apply {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            addJavascriptInterface(
                PTWebViewInterface { response ->
                    when (response.type) {
                        RegistrationResponse.ATTENDEE_AUTHENTICATED -> {
                            delayedAuthenticationJob?.cancel()
                            delayedAuthenticationJob = null
                            if (authenticated) {
                                checkCompletion()
                                return@PTWebViewInterface
                            }
                            // Yes, we set this first, not last, to block re-entrance.
                            authenticated = true
                            val registrationData: RegistrationData =
                                KiboSerializable.decodeFromJsonElement(response.data)
                            trackUserRegistrationEvent()
                            viewModel.saveAndSendRegistrationData(registrationData)
                            checkCompletion()
                        }

                        RegistrationResponse.ACTIVATION_COMPLETE, RegistrationResponse.DEVICE_LINKING_COMPLETE -> {
                            if (activationComplete) {
                                checkCompletion()
                                return@PTWebViewInterface
                            }
                            // We set this first to block re-entrance.
                            activationComplete = true
                            if (!authenticated && delayedAuthenticationJob == null) {
                                try {
                                    val completionData: CompletionData =
                                        KiboSerializable.decodeFromJsonElement(response.data)
                                    if (!completionData.attendeeHasAuthenticated) {
                                        // If we reach here, it means that we have an activation
                                        // that doesn't have authentication. We'll never receive
                                        // the ATTENDEE_AUTHENTICATED event, so we simulate
                                        // authentication and let things proceed.
                                        authenticated = true
                                    }
                                } catch (_: SerializationException) {
                                    // Since we don't have the payload, we have no
                                    // idea whether we'll get an ATTENDEE_AUTHENTICATED
                                    // event or not. So we wait 1 second and then
                                    // simulate authentication so we can move forward.
                                    delayedAuthenticationJob = viewModel.viewModelScope.launch {
                                        delay(1000)
                                        withContext(Dispatchers.Main) {
                                            authenticated = true
                                            checkCompletion()
                                        }
                                    }
                                }
                            }
                            checkCompletion()
                        }

                        RegistrationResponse.ACTIVATION_RESTARTED, RegistrationResponse.DEVICE_LINKING_RESTARTED ->
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                startRegistration()
                            }
                    }
                },
                JS_INTERFACE_NAME
            )
        }
    }

    private fun checkCompletion() {
        if (registrationComplete || !activationComplete || !authenticated) return
        registrationComplete = true
        viewModel.completeRegistration()
        data.onSuccessFeatureInfo?.let {
            val routeController: RouteController = App.resolve()
            routeController.replaceBackStackAware(this@RegistrationFragment, it)
        } ?: data.onboardingPageLayoutData?.let {
            onboardingPageDelegate?.pageDidComplete(onboardingPageId, true)
        } ?: activity?.runOnUiThread {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private suspend fun startRegistration() {
        viewModel.prepareForRegistration(data).let { prepareResult ->
            withContext(Dispatchers.Main) {
                injectCookiesAndLoadUrl(prepareResult)
            }
        }
    }

    private fun injectCookiesAndLoadUrl(result: PrepareResult) {
        if (result.cookies.isEmpty()) {
            binding.webview.loadUrl(result.url)
            return
        }
        binding.webview.clearCache(false)
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(binding.webview, true)

            result.cookies.forEachIndexed { index, cookie ->
                setCookie(result.url, cookie) {
                    if (index == result.cookies.lastIndex) {
                        binding.webview.loadUrl(result.url)
                    }
                }
            }
        }
    }

    fun updateUrl(url: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.webview.loadUrl(url)
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.registration()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webview.removeJavascriptInterface(JS_INTERFACE_NAME)
    }

    private fun Screen.Companion.registration(): Screen =
        Screen(data.analytics.screenName, "thuzi_registration")

    override fun restoreData(encodedData: String): RegistrationLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private fun trackUserRegistrationEvent() {
        App.track(UserRegistrationEvent(data.getFeatureParamsAnalytics()?.screenName))
    }

    internal data class UserRegistrationEvent(
        private val itemCategory: String?,
    ) : MappedMetrics {
        override fun track(provider: MappedProvider) {
            val eventName = EventName.thuziRegistration
            val parameters = itemCategory?.let {
                mapOf(EventParameter.itemCategory to it)
            } ?: mapOf()
            provider.track(eventName, parameters)
        }
    }

    override val onboardingScreenViewEvent: ScreenViewEvent? by lazy {
        data.onboardingPageLayoutData?.onboardingAnalytics?.let {
            val screen = Screen.registration()
            val parameters =
                mapOf(EventParameter.itemCategory to it.featureName.plus(" Onboarding"))
            ScreenViewEvent(screen, parameters)
        }
    }
    override val onboardingPageId: String by lazy {
        data.onboardingPageLayoutData?.pageId ?: throw createPageIdMissingException()
    }
}
