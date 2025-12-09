package com.greencopper.interfacekit.sample.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.SampleFragmentBinding
import com.greencopper.interfacekit.metrics.sample
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.sample.SampleLayoutData
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.toolkit.App

internal class SampleFragment : ParameterizedFragment<SampleLayoutData>, RedirectableLayout {
    constructor(constructorData: SampleLayoutData) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val screenColor: ScreenColor get() = InterfaceKitColor.sample
    override val binding: SampleFragmentBinding by viewBinding(SampleFragmentBinding::inflate)

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.sampleToolbar,
            screenColor.topBar,
            InterfaceKitTextStyle.sample.topBar,
            title = "Sample",
        )

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with (binding) {
            sampleIv.setColorFilter(InterfaceKitColor.sample.icon)
            sampleTv.setTextColor(InterfaceKitColor.sample.text)
            root.setBackgroundColor(InterfaceKitColor.sample.background)

            sampleIv.setImageFrom(data.imageName, viewLifecycleOwner.lifecycleScope)
            sampleTv.text = data.text

            binding.tvLargeTitle.setFont(InterfaceKitTextStyle.sample.largeTitle)
            binding.tvTitleXL.setFont(InterfaceKitTextStyle.sample.titleXL)
            binding.tvTitleL.setFont(InterfaceKitTextStyle.sample.titleL)
            binding.tvTitleM.setFont(InterfaceKitTextStyle.sample.titleM)
            binding.tvTitleS.setFont(InterfaceKitTextStyle.sample.titleS)
            binding.tvTitleXS.setFont(InterfaceKitTextStyle.sample.titleXS)
            binding.tvHeadlineL.setFont(InterfaceKitTextStyle.sample.headlineL)
            binding.tvHeadlineM.setFont(InterfaceKitTextStyle.sample.headlineM)
            binding.tvHeadlineS.setFont(InterfaceKitTextStyle.sample.headlineS)
            binding.tvBodyXL.setFont(InterfaceKitTextStyle.sample.bodyXL)
            binding.tvBodyL.setFont(InterfaceKitTextStyle.sample.bodyL)
            binding.tvBodyM.setFont(InterfaceKitTextStyle.sample.bodyM)
            binding.tvBodyS.setFont(InterfaceKitTextStyle.sample.bodyS)
            binding.tvBodyXS.setFont(InterfaceKitTextStyle.sample.bodyXS)
            binding.tvCaptionL.setFont(InterfaceKitTextStyle.sample.captionL)
            binding.tvCaptionS.setFont(InterfaceKitTextStyle.sample.captionS)
            binding.tvFootnoteM.setFont(InterfaceKitTextStyle.sample.footnoteM)
            binding.tvFootnoteS.setFont(InterfaceKitTextStyle.sample.footnoteS)
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.sample(data.text)))
    }

    override fun restoreData(encodedData: String): SampleLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
