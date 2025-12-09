package com.greencopper.interfacekit.widgets.ui.widgetcollection

import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.WidgetCollectionFragmentBinding
import com.greencopper.interfacekit.metrics.widgetCollection
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.*
import com.greencopper.interfacekit.viewModel
import com.greencopper.interfacekit.widgets.WidgetCollectionLayoutData
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.interfacekit.widgets.viewmodel.widgetcollection.WidgetCollectionViewModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.core.graphics.drawable.toDrawable

internal class WidgetCollectionFragment : ParameterizedFragment<WidgetCollectionLayoutData>, RedirectableLayout {

    constructor(params: WidgetCollectionLayoutData) : super(params)

    @Deprecated(
        "To conform to system instantiation of any fragment. To read more about it check " +
                "Fragment.java instantiate method or FragmentFactory instantiate"
    )
    constructor() : super(null)

    override val screenColor: ScreenColor get() = InterfaceKitColor.widgetCollection

    override val binding: WidgetCollectionFragmentBinding by viewBinding(
        WidgetCollectionFragmentBinding::inflate
    )

    private val localizationService: LocalizationService by App.lazy()

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    private val viewModel: WidgetCollectionViewModel by viewModel()

    override fun createNavigationControlsHandler(): NavigationControlsHandler {
        val useButtonNav = data.header != null && data.topBar == null
        return if (useButtonNav) {
            DefaultButtonsNavigationControlsHandler(
                this,
                binding.navigateBackButton,
                binding.navigateCloseButton,
                InterfaceKitColor.widgetCollection.topBar
            )
        } else {
            DefaultBackCloseToolbarNavigationControlsHandler(
                this,
                binding.backCloseButtonToolbar,
                InterfaceKitColor.widgetCollection.topBar,
                InterfaceKitTextStyle.widgetCollection.topBar,
                data.topBar?.title?.let { localizationService.getString(it) },
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.widgetCollectionRecyclerView.bind(
                    data.header?.let { WidgetCollectionView.HeaderItem(it) },
                    viewModel.getWidgetItems(data.widgets),
                    this@WidgetCollectionFragment,
                    data.analytics.screenName,
                ).collect()
            }
        }
        binding.root.background = InterfaceKitColor.widgetCollection.background.toDrawable()
        binding.widgetCollectionRecyclerView.itemAnimator = null

        data.topBar?.let {
            binding.backCloseButtonToolbar.setupTopBarData(it, this@WidgetCollectionFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.widgetCollection(data.analytics.screenName)))
    }

    override fun restoreData(encodedData: String): WidgetCollectionLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
