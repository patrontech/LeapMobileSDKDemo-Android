package com.greencopper.interfacekit.tabBar.viewmodel

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.service.MappedMetadataService
import com.greencopper.interfacekit.metrics.tabBarCurrentTab
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectingLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.tabBar.TabBarData
import com.greencopper.interfacekit.tabBar.TabBarLayoutData
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.ui.shouldColorNavigationBar
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.toggl.komposable.architecture.Store
import kotlin.collections.flatMap

internal class TabBarViewModel(
    val viewBuilder: IKViewBuilder,
    val store: Store<TabBarState, TabBarAction>,
    private val featureResolver: FeatureResolver,
    private val mappedMetadataService: MappedMetadataService,
) : ViewModel() {

    fun getAvailableLayouts(data: TabBarLayoutData): List<DialogFragment?> =
        data.items.map { item ->
            item.display.getFeatureInfo()?.let { getFragmentLayout(it) }
        }

    fun getAvailableRedirections(data: TabBarLayoutData): List<RedirectionHash> =
        getAvailableLayouts(data)
            .flatMap {
                when (it) {
                    is RedirectableLayout -> listOf(it.redirectionHash)
                    is RedirectingLayout -> it.availableRedirections
                    else -> emptyList()
                }
            }

    fun getFragmentLayout(feature: FeatureInfo): Layout? {
        return try {
            featureResolver.resolve(feature).apply {
                shouldColorNavigationBar = false
            }
        } catch (t: Throwable) {
            App.log.e("Couldn't resolve feature $feature", throwable = t)
            null
        }
    }

    fun retainCurrentTabName(name: String) {
        mappedMetadataService[EventParameter.tabBarCurrentTab] = name
    }
}

private fun TabBarData.Display.getFeatureInfo(): FeatureInfo? {
    return when (this) {
        is TabBarData.Display.Embedded -> {
            this.feature
        }

        is TabBarData.Display.Routing -> {
            when (this.route) {
                is Route.Push -> route.feature
                is Route.Present -> route.feature
                is Route.External, is Route.Execute -> null
            }
        }
    }
}
