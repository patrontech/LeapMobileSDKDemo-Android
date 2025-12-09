package com.greencopper.ticketing.providers.showclix.login.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.*
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.SafeClickListener
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.textstyle.subsystem.setErrorFont
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.ticketing.R
import com.greencopper.ticketing.TicketingColor
import com.greencopper.ticketing.databinding.ShowclixLoginFragmentBinding
import com.greencopper.ticketing.metrics.showclixLogin
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginOnboardingLayoutData
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginViewModel
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.ticketing.textstyle.TicketingTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.extensions.getSerializableCompat
import kotlinx.coroutines.launch

internal class ShowclixLoginFragment :
    ParameterizedFragment<ShowclixLoginOnboardingLayoutData>,
    OnboardingPageLayout {

    constructor(constructorData: ShowclixLoginOnboardingLayoutData?) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    private val localizationService: LocalizationService by lazy { App.resolve() }
    private val routeController: RouteController by lazy { App.resolve() }
    private val localStorage: LocalStorage by lazy { App.resolve() }
    private val metricService: AggregateMetricsService by lazy { App.resolve() }
    private var errorIsShown: Boolean = false

    override val binding: ShowclixLoginFragmentBinding by viewBinding(
        ShowclixLoginFragmentBinding::inflate
    )
    override val screenColor: ScreenColor get() = TicketingColor.showclixLogin

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.showclixToolbar,
            TicketingColor.showclixLogin.topBar,
            TicketingTextStyle.showclixLogin.topBar,
        )

    private val showclixViewModel: ShowclixLoginViewModel by viewModel()

    override val onboardingScreenViewEvent: ScreenViewEvent? by lazy {
        data.onboardingPageLayoutData.onboardingAnalytics?.let {
            ScreenViewEvent(
                Screen(it.screenName, "${it.featureName}")
            )
        }
    }

    override val onboardingPageId: String by lazy {
        data.onboardingPageLayoutData.pageId
    }

    private var email: String? = null
    private var currentState: UIState = UIState.ENTER_EMAIL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            email = savedInstanceState.getString(SAVE_KEY_EMAIL, "")
            currentState = savedInstanceState.getSerializableCompat(SAVE_KEY_STATE, UIState::class.java) as UIState
        } else {
            email = with(localStorage.project.user.email.value) {
                get(Email.SHOWCLIX.key)
                    ?: get(Email.THUZI.key)
                    ?: get(Email.TICKETMASTER.key)
            } ?: ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindStyles()

        with(binding) {
            email?.let {
                tieEmail.setText(it)
                tvEmailSent.text = it
            }
            tieEmail.doOnTextChanged { _, _, _, _ ->
                isShowingEmailInputError(false)
            }
            tieEmail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    checkEmailAndSend()
                }
                false
            }
            ivMainImage.setImageFrom(data.images.enterEmail, lifecycleScope)

            tvChangeEmail.text =
                localizationService.getString("ticketing.showclix.login.email_sent.change_email")
            tvChangeEmail.setOnClickListener {
                switchUIState(UIState.ENTER_EMAIL)
            }
        }

        localStorage.project.showclix.timeToken.value?.let {
            localStorage.project.showclix.timeToken.value = null
            verifyToken(it)
        } ?: switchUIState(currentState)
    }

    override fun onResume() {
        super.onResume()
        data.onboardingPageLayoutData.onboardingAnalytics?.screenName?.let {
            metricService.track(ScreenViewEvent(Screen.showclixLogin(it)))
        }
    }

    private val checkEmailAndSendClickListener = SafeClickListener {
        checkEmailAndSend()
    }

    private fun checkEmailAndSend() {
        val email = binding.tieEmail.text.toString().trim().lowercase()
        if (showclixViewModel.isEmailFormatValid(email)) {
            activity?.currentFocus?.let { view ->
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
            binding.tieEmail.clearFocus()
            switchUIState(UIState.LOADING_SEND_MAGIC_LINK)
            lifecycleScope.launch {
                if (showclixViewModel.sendMagicLink(email, data.apiUrl, data.magicLink)) {
                    binding.tvEmailSent.text = email
                    switchUIState(UIState.EMAIL_SENT)
                } else {
                    routeController.showAlert(
                        title = localizationService.getString("common.an_error_occured"),
                        message = localizationService.getString("ticketing.showclix.login.sending_email.error.message"),
                        positiveText = localizationService.getString("common.ok"),
                        onPositiveClicked = {
                            switchUIState(UIState.ENTER_EMAIL)
                        }
                    )
                }
            }
        } else {
            isShowingEmailInputError(true)
        }
    }

    private val resendMagicLinkClickListener = SafeClickListener {
        resendMagicLink()
    }

    private fun resendMagicLink() {
        switchUIState(UIState.LOADING_SEND_MAGIC_LINK)
        lifecycleScope.launch {
            if (showclixViewModel.sendMagicLink(
                    binding.tieEmail.text.toString().trim().lowercase(),
                    data.apiUrl,
                    data.magicLink
                )
            ) {
                switchUIState(UIState.EMAIL_RESENT)
            } else {
                routeController.showAlert(
                    title = localizationService.getString("common.an_error_occured"),
                    message = localizationService.getString("ticketing.showclix.login.sending_email.error.message"),
                    positiveText = localizationService.getString("common.ok"),
                    onPositiveClicked = {
                        switchUIState(UIState.ENTER_EMAIL)
                    }
                )
            }
        }
    }

    private fun switchUIState(state: UIState) {
        currentState = state
        with(binding) {
            when (state) {
                UIState.ENTER_EMAIL -> {
                    tvEmailSent.visibility = View.GONE
                    tvChangeEmail.visibility = View.GONE
                    progressLoader.visibility = View.GONE
                    ivMainImage.setImageFrom(data.images.enterEmail, lifecycleScope)
                    tvTitle.text =
                        localizationService.getString("ticketing.showclix.login.enter_email.title")
                    tvSubtitle.text =
                        localizationService.getString("ticketing.showclix.login.enter_email.subtitle")
                    leapbuttonCta.text =
                        localizationService.getString("ticketing.showclix.login.enter_email.get_link")
                    leapbuttonCta.setOnClickListener(checkEmailAndSendClickListener)
                    leapbuttonCta.visibility = View.VISIBLE
                    tilEmail.visibility = View.VISIBLE
                    leapbuttonCta.visibility = View.VISIBLE
                }

                UIState.LOADING_SEND_MAGIC_LINK, UIState.LOADING_VALIDATING_TOKEN -> {
                    tilEmail.visibility = View.INVISIBLE
                    tvEmailSent.visibility = View.GONE
                    leapbuttonCta.visibility = View.GONE
                    tvChangeEmail.visibility = View.GONE
                    if (state == UIState.LOADING_SEND_MAGIC_LINK) {
                        tvTitle.text =
                            localizationService.getString("ticketing.showclix.login.sending_email.title")
                        tvSubtitle.text =
                            localizationService.getString("ticketing.showclix.login.sending_email.subtitle")
                    } else {
                        tvTitle.text =
                            localizationService.getString("ticketing.showclix.login.validating_account.title")
                        tvSubtitle.text =
                            localizationService.getString("ticketing.showclix.login.validating_account.subtitle")
                    }
                    progressLoader.visibility = View.VISIBLE
                }

                UIState.EMAIL_SENT -> {
                    tilEmail.visibility = View.INVISIBLE
                    progressLoader.visibility = View.GONE
                    ivMainImage.setImageFrom(data.images.emailSent, lifecycleScope)
                    tvTitle.text =
                        localizationService.getString("ticketing.showclix.login.email_sent.title")
                    tvSubtitle.text =
                        localizationService.getString("ticketing.showclix.login.email_sent.subtitle")
                    leapbuttonCta.text =
                        localizationService.getString("ticketing.showclix.login.email_sent.resend_link")
                    leapbuttonCta.setOnClickListener(resendMagicLinkClickListener)
                    tvEmailSent.visibility = View.VISIBLE
                    leapbuttonCta.visibility = View.VISIBLE
                    tvChangeEmail.visibility = View.VISIBLE
                }

                UIState.EMAIL_RESENT -> {
                    tilEmail.visibility = View.INVISIBLE
                    progressLoader.visibility = View.GONE
                    ivMainImage.setImageFrom(data.images.emailSent, lifecycleScope)
                    tvTitle.text =
                        localizationService.getString("ticketing.showclix.login.email_resent.title")
                    tvSubtitle.text =
                        localizationService.getString("ticketing.showclix.login.email_resent.subtitle")
                    leapbuttonCta.text =
                        localizationService.getString("ticketing.showclix.login.email_sent.resend_link")
                    leapbuttonCta.setOnClickListener(resendMagicLinkClickListener)
                    tvEmailSent.visibility = View.VISIBLE
                    leapbuttonCta.visibility = View.VISIBLE
                    tvChangeEmail.visibility = View.VISIBLE
                }
            }
        }
    }

    fun verifyToken(token: String) {
        lifecycleScope.launch {
            switchUIState(UIState.LOADING_VALIDATING_TOKEN)
            if (showclixViewModel.verifyToken(data.apiUrl, token)) {
                onboardingPageDelegate?.pageDidComplete(onboardingPageId, true)
            } else {
                routeController.showAlert(
                    title = localizationService.getString("common.an_error_occured"),
                    message = localizationService.getString("ticketing.showclix.login.validating_account.error.message"),
                    positiveText = localizationService.getString("common.ok"),
                    onPositiveClicked = {
                        if (localStorage.project.user.email.value[Email.SHOWCLIX.key] == null) {
                            switchUIState(UIState.ENTER_EMAIL)
                        } else {
                            switchUIState(UIState.EMAIL_SENT)
                        }
                    }
                )
            }
        }
    }

    private fun bindStyles() {
        val colors = TicketingColor.showclixLogin
        val textStyle = TicketingTextStyle.showclixLogin
        with(binding) {
            root.setBackgroundColor(colors.background)
            tvTitle.apply {
                setTextColor(colors.title)
                setFont(textStyle.title)
            }
            tvSubtitle.apply {
                setTextColor(colors.subtitle)
                setFont(textStyle.subtitle)
            }
            tilEmail.apply {
                setFont(tieEmail, textStyle.emailTextField.label)
                setErrorTextColor(ColorStateList.valueOf(colors.emailTextField.error))
                setErrorIconTintList(ColorStateList.valueOf(colors.emailTextField.error))
                boxStrokeErrorColor = ColorStateList.valueOf(colors.emailTextField.error)
            }
            tvEmailSent.apply {
                setTextColor(colors.email)
                setFont(textStyle.email)
            }
            leapbuttonCta.apply {
                setFont(textStyle.button)
                setTextColor(colors.button.label)
                setBackgroundColor(colors.button.background)
            }
            tvChangeEmail.apply {
                setTextColor(colors.changeEmail)
                setFont(textStyle.changeEmail)
            }
            (tieEmail.background as? GradientDrawable)?.color =
                ColorStateList.valueOf(colors.emailTextField.background)
            progressLoader.indeterminateTintList = ColorStateList.valueOf(colors.loader)
        }
        isShowingEmailInputError(false)
    }

    private fun isShowingEmailInputError(isShowing: Boolean) {
        val colorsEmailTextField = TicketingColor.showclixLogin.emailTextField
        if (isShowing) {
            if (!errorIsShown) {
                errorIsShown = true
                binding.tilEmail.apply {
                    setStartIconDrawable(R.drawable.ic_info)
                    setStartIconTintList(ColorStateList.valueOf(colorsEmailTextField.error))
                    setErrorFont(
                        localizationService.getString("ticketing.showclix.login.enter_email.error.invalid_email"),
                        TicketingTextStyle.showclixLogin.emailTextField.error,
                    )
                }
                binding.tieEmail.apply {
                    setTextColor(colorsEmailTextField.error)
                    (background?.mutate() as? GradientDrawable)?.setStroke(
                        1.dpToPx(),
                        ColorStateList.valueOf(colorsEmailTextField.error)
                    )
                }

                binding.root.findViewById<AppCompatTextView>(com.google.android.material.R.id.textinput_error)?.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        } else {
            errorIsShown = false
            binding.tilEmail.apply {
                setStartIconDrawable(R.drawable.ic_email)
                setStartIconTintList(ColorStateList.valueOf(colorsEmailTextField.icon))
                error = null
            }
            binding.tieEmail.apply {
                setTextColor(colorsEmailTextField.label)
                (background as? GradientDrawable)?.setStroke(
                    1.dpToPx(),
                    ColorStateList.valueOf(colorsEmailTextField.border).withAlpha(40)
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVE_KEY_EMAIL, binding.tieEmail.text.toString())
        outState.putSerializable(SAVE_KEY_STATE, currentState)
    }

    override fun restoreData(encodedData: String): ShowclixLoginOnboardingLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    enum class UIState {
        ENTER_EMAIL,
        EMAIL_SENT,
        EMAIL_RESENT,
        LOADING_SEND_MAGIC_LINK,
        LOADING_VALIDATING_TOKEN
    }

    companion object {
        private const val SAVE_KEY_EMAIL = "save_key_email"
        private const val SAVE_KEY_STATE = "save_key_state"
    }
}
