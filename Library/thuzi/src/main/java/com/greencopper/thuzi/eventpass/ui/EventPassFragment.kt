package com.greencopper.thuzi.eventpass.ui

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.thuzi.style.ThuziColor
import com.greencopper.thuzi.databinding.EventpassFragmentBinding
import com.greencopper.thuzi.eventpass.EventPassViewModel
import com.greencopper.thuzi.eventpass.initializer.EventPassLayoutData
import com.greencopper.thuzi.metrics.eventPass
import com.greencopper.thuzi.style.ThuziTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.launch

internal class EventPassFragment : ParameterizedFragment<EventPassLayoutData>, RedirectableLayout {

    constructor(eventPassData: EventPassLayoutData) : super(eventPassData)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash
    override val binding: EventpassFragmentBinding by viewBinding(EventpassFragmentBinding::inflate)
    override val screenColor: ScreenColor get() = ThuziColor.eventPass
    private val localizationService: LocalizationService by lazy { App.resolve() }

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            ThuziColor.eventPass.topBar
        )

    private val viewModel: EventPassViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.getBarcodeData().collect {
                updateBarcodeDisplay(it)
            }
        }
        setupView()
    }

    private fun setupView() {
        with(binding) {
            root.setBackgroundColor(ThuziColor.eventPass.background)

            coloredBackground.background.setTint(ThuziColor.eventPass.header.background)

            val headerColorStyle = ThuziColor.eventPass.header
            val headerTextStyle = ThuziTextStyle.eventPass.header
            with(title) {
                setTextColor(headerColorStyle.title)
                setFont(headerTextStyle.title)
                setOtaText("thuzi.eventpass.title")
            }
            with(description) {
                setTextColor(headerColorStyle.description)
                setFont(headerTextStyle.description)
                setOtaText("thuzi.eventpass.description")
            }

            with(cardView) {
                setShadowColor(ThuziColor.eventPass.card.shadow)
                strokeColor = ThuziColor.eventPass.card.border
            }

            qrCodeView.contentDescription = localizationService.getString("thuzi.eventpass.qr_code.label")

            navigateCloseButton.doOnPreDraw {
                title.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    setMargins(16.dpToPx(),
                        (it.bottom - resources.getDimension(R.dimen.navigate_button_margin) + 4.dpToPx()).toInt(),
                        16.dpToPx(),
                        0)
                }
            }

        }
    }

    private fun updateBarcodeDisplay(barcodeViewData: EventPassViewModel.BarcodeViewData) {
        binding.apply {
            qrCodeView.setBarcodeValue(barcodeViewData.barcodeValue)
            qrCodeTextValue.text = barcodeViewData.barcodeValue

            userFirstNameTextValue.text = barcodeViewData.userFirstName
            userFirstNameTextValue.visibility = if (barcodeViewData.userFirstName.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.eventPass(data.analytics.screenName)))
    }

    override fun restoreData(encodedData: String): EventPassLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
