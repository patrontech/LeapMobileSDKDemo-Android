package com.greencopper.interfacekit.editorial.ui

import android.os.Bundle
import android.view.View
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.editorial.EditorialPageLayoutData
import com.greencopper.interfacekit.metrics.editorialPage
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.webview.ui.BaseWebViewFragment
import com.greencopper.toolkit.App

internal class EditorialPageFragment
    : BaseWebViewFragment<EditorialPageLayoutData>, RedirectableLayout {

    constructor(params: EditorialPageLayoutData) : super(params)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with (binding.webview) {
            settings.allowFileAccess = true
            super.onViewCreated(view, savedInstanceState)
            settings.builtInZoomControls = false
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.editorialPage(data.analytics.screenName)))
    }

    override fun restoreData(encodedData: String): EditorialPageLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}