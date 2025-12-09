package com.greencopper.interfacekit.tabBar.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.*
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.interfacekit.*
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.KeyboardHelper
import com.greencopper.interfacekit.databinding.TabBarFragmentBinding
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.navigation.layout.*
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.tabBar.TabBarData
import com.greencopper.interfacekit.tabBar.TabBarLayoutData
import com.greencopper.interfacekit.tabBar.viewmodel.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

internal class TabBarFragment : ParameterizedFragment<TabBarLayoutData>,
    NavigationController<TabBarFragment>,
    RedirectingLayout,
    RedirectableLayout {

    constructor(constructorData: TabBarLayoutData) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val navigationBarColor: Int by lazy { InterfaceKitColor.bottomBar.background }
    override val screenColor: ScreenColor? get() = null
    override val binding: TabBarFragmentBinding by viewBinding(TabBarFragmentBinding::inflate)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val availableRedirections: List<RedirectionHash>
        get() = savedState?.let { viewModel.getAvailableRedirections(data) } ?: emptyList()

    private val viewModel: TabBarViewModel by viewModel { listOf(
        savedState ?: TabBarState(-1, emptyList())
    ) }
    private val routeController: RouteController by App.lazy()
    private val localizationService: LocalizationService by App.lazy()

    private val keyboardHelper: KeyboardHelper = KeyboardHelper()
    private val keyboardListener = object : KeyboardHelper.KeyboardListener {
        override fun onKeyboardShowing() {
            if (isAdded) {
                binding.tabBarComposeView.visibility = View.GONE
            }
        }

        override fun onKeyboardHiding() {
            if (isAdded) {
                binding.tabBarComposeView.visibility = View.VISIBLE
            }
        }
    }

    private var savedState: TabBarState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedState = savedInstanceState?.getKiboSerializable<TabBarState>(SAVED_STATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabBarComposeView.setContent {
            viewModel.viewBuilder.buildContent {
                val viewState = viewModel.store.state.collectAsStateWithLifecycle(null)
                viewState.value?.let {
                    BottomTabBar(it) { action ->
                        viewModel.store.send(action)
                    }
                }
            }
        }

        collectTabBarState()

        viewModel.store.send(TabBarAction.LoadInitialTabData(data))
        keyboardHelper.setKeyboardAppearanceListener(requireActivity().window, keyboardListener)

        ncChildFragmentManager.addOnBackStackChangedListener {
            val tag = ncChildFragmentManager.getBackStackEntryAt(ncChildFragmentManager.backStackEntryCount - 1).name.orEmpty()
            viewModel.store.send(TabBarAction.TabSelectedByTag(tag))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putKiboSerializable(SAVED_STATE, savedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        keyboardHelper.removeListener(requireActivity().window)
    }

    override fun redirectTo(hash: RedirectionHash) {
        viewModel.getAvailableLayouts(data).forEachIndexed { index, layout ->
            when {
                layout is RedirectableLayout && layout.redirectionHash == hash -> {
                    viewModel.store.send(TabBarAction.TabRedirected(index))
                }

                layout is RedirectingLayout && layout.availableRedirections.contains(hash) -> {
                    viewModel.store.send(TabBarAction.TabRedirected(index))
                }
            }
        }
    }

    override fun getContainerId() = R.id.tabbar_fragment_container

    private fun collectTabBarState() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.store.state.distinctUntilChanged().collect { tabBarState ->
            // The state will only be the same when the view is restoring itself after onSaveInstanceState.
            // We don't need to re-embed the fragment, since it's already taken care of restoring itself.
            val changeTab = savedState != tabBarState
            savedState = tabBarState
            if (tabBarState.selectedIndex >= 0 && changeTab) {
                embedMenuItem(data.items[tabBarState.selectedIndex])
            }
        }
    }
        
    private fun embedMenuItem(item: TabBarData.Item): Layout? {
        lifecycleScope.launch {
            if (data.trackMetadata == true) {
                viewModel.retainCurrentTabName(item.analytics.itemName)
            }
        }

        when (val display = item.display) {
            is TabBarData.Display.Embedded -> {
                val layout: Layout? = viewModel.getFragmentLayout(display.feature)
                if (layout == null) {
                    showMenuItemError(
                        localizationService.getString("interfaceKit.unavailable_feature.title"),
                        localizationService.getString("interfaceKit.unavailable_feature.message")
                    )
                    return null
                }

                val tag = localizationService.getString(item.name)

                if (!childFragmentManager.contains(tag)) {
                    // The fragment is nowhere in the backStack so we'll embed it.
                    childFragmentManager.replace(
                        containerLayoutId = binding.tabbarFragmentContainer.id,
                        fragment = layout,
                        tag = tag,
                        true
                    )
                } else {
                    // The fragment is somewhere in the backStack, pop back to it
                    childFragmentManager.popBackStackImmediate(tag, 0)
                }

                return layout
            }

            is TabBarData.Display.Routing -> {
                routeTo(display.route)
                return null
            }
        }
    }

    private fun routeTo(route: Route) {
        when (route) {
            is Route.Push -> showMenuItemError(
                localizationService.getString("common.an_error_occured"),
                getString(R.string.interfaceKit__push_error__message)
            )

            is Route.Present -> routeController.resolve(route, this)
            is Route.External -> routeController.resolve(route, this)
            is Route.Execute -> routeController.resolve(route, this)
        }
    }

    override fun restoreData(encodedData: String): TabBarLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private fun showMenuItemError(title: String, text: String) {
        routeController.showAlert(title, text)
    }

    private companion object {
        const val SAVED_STATE = "tabBarFragmentSavedState"
    }
}

private fun FragmentManager.contains(tag: String): Boolean {
    for (i in 0 until backStackEntryCount) {
        val backStackFragment = getBackStackEntryAt(i)
        if (backStackFragment.name == tag) {
            return true
        }
    }
    return false
}

internal data class TabBarTapEvent(
    private val itemName: String,
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        val eventName = EventName("tab_bar/tab_tap")
        val parameters = mapOf(EventParameter.itemName to itemName)
        provider.track(eventName, parameters)
    }
}
