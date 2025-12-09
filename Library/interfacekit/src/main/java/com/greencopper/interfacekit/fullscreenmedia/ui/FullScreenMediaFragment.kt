package com.greencopper.interfacekit.fullscreenmedia.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor.fullScreenMedia
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.databinding.FullScreenMediaFragmentBinding
import com.greencopper.interfacekit.fullscreenmedia.FullScreenMediaLayoutData
import com.greencopper.interfacekit.metrics.fullScreenMedia
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.toolkit.App

internal class FullScreenMediaFragment : ParameterizedFragment<FullScreenMediaLayoutData>,
    RedirectableLayout {

    constructor(constructorData: FullScreenMediaLayoutData) : super(constructorData)

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            fullScreenMedia.topBar
        )

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val binding: FullScreenMediaFragmentBinding by viewBinding(
        FullScreenMediaFragmentBinding::inflate
    )
    override val screenColor: ScreenColor get() = fullScreenMedia

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setBackgroundColor(fullScreenMedia.background)
        binding.imageFullScreen.setImageFrom(
            name = data.mediaName,
            lifecycleScope = viewLifecycleOwner.lifecycleScope
        )
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.fullScreenMedia(data.analytics.screenName)))
    }

    override fun restoreData(encodedData: String): FullScreenMediaLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
