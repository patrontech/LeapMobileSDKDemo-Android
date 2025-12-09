package com.greencopper.thuzi.microsite.ui

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.webview.ui.BaseWebViewFragment
import com.greencopper.thuzi.microsite.MicrositeLayoutData
import com.greencopper.toolkit.App

internal class MicrositeFragment : BaseWebViewFragment<MicrositeLayoutData>, RedirectableLayout {

    constructor(micrositeLayoutData: MicrositeLayoutData) : super(micrositeLayoutData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.microsite))
    }

    override fun restoreData(encodedData: String): MicrositeLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private val Screen.Companion.microsite: Screen
        get() = Screen(data.analytics.screenName, "thuzi_microsite")
}
