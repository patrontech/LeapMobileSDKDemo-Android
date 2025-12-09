package com.greencopper.interfacekit.lists.ui

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import com.greencopper.core.data.getKiboSerializable
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.databinding.BaseListFragmentBinding
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBar
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarCell
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarColor
import com.greencopper.interfacekit.filtering.filteringbar.ui.FilteringBarTextStyle
import com.greencopper.interfacekit.lists.ListData
import com.greencopper.interfacekit.lists.ListViewModel
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.subsystem.TopBarTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.SimpleLineDecorator
import com.greencopper.interfacekit.ui.WidgetCollectionCellAwareItemDecorator
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.fragment.launchRepeatingJob
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.JobAwareViewHolder
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

public abstract class ListFragment<T : ListData<T>>(listData: T?) : ParameterizedFragment<T>(listData),
    RedirectableLayout {

    protected val localizationService: LocalizationService by App.lazy()
    protected val routeController: RouteController by App.lazy()

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    protected abstract val backgroundColor: Int
    protected abstract val topBarColor: TopBarColor
    protected abstract val topBarTextStyle: TopBarTextStyle
    protected abstract val defaultTopBarTitle: String
    protected abstract val filteringBarColors: FilteringBarColor
    protected abstract val filteringBarTextStyle: FilteringBarTextStyle

    protected abstract val emptyViewColors: EmptyViewColors
    protected abstract val emptyViewTextStyle: EmptyViewTextStyle
    protected abstract var listAdapter: ListAdapter<*, JobAwareViewHolder>

    protected var savedFiltering: ListViewModel.SavedFiltering? = null

    protected abstract val viewModel: ListViewModel<*>

    protected abstract val decoratorInfo: SimpleLineDecorator

    protected abstract val screenAnalytics: ScreenViewEvent

    override val binding: BaseListFragmentBinding by viewBinding(BaseListFragmentBinding::inflate)

    protected fun computeFilteringModes(): Map<FilteringHandler.Mode, FilteringInfo?> {
        val filteringModes = mutableMapOf<FilteringHandler.Mode, FilteringInfo?>()

        filteringModes[FilteringHandler.Mode.DEFAULT] = data.filtering
        filteringModes[FilteringHandler.Mode.MY_FAVORITES] = data.myFavorites?.filtering

        return filteringModes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedFiltering =
            savedInstanceState?.getKiboSerializable<ListViewModel.SavedFiltering>(SAVED_FILTERING_KEY)
    }

    protected abstract fun setupAdapter()

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.baseListToolbar,
            topBarColor,
            topBarTextStyle,
            localizationService.getString(data.title ?: defaultTopBarTitle),
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchIcon()
        setupAdapter()

        binding.root.setBackgroundColor(backgroundColor)
        binding.baseListRecyclerView.apply {
            adapter = listAdapter
            itemAnimator = null
            addItemDecoration(WidgetCollectionCellAwareItemDecorator(decoratorInfo, true))
        }

        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.getItems(data.widgetCollections)
                .flowOn(Dispatchers.IO)
                .collectLatest { items ->
                    listAdapter.submitList(items as List<Nothing>?)
                    toggleEmptyView(items.isEmpty() && viewModel.getMode() == FilteringHandler.Mode.MY_FAVORITES)
                    setupFilteringBar()
                }
        }

        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.filterChangingNotifier
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    binding.baseListRecyclerView.scrollToPosition(0)
                }
        }

        savedFiltering?.let {
            viewModel.switchMode(it.mode)
        }
    }

    private fun setupFilteringBar() {
        with(binding.baseListFilteringBar) {
            if (!isSetup) {
                binding.baseListToolbar.elevation = 0f
                with(binding.baseListFilteringBarBorderTop) {
                    isVisible = true
                    setBackgroundColor(filteringBarColors.border)
                }
                with(binding.baseListFilteringBarBorderBottom) {
                    isVisible = true
                    setBackgroundColor(filteringBarColors.border)
                }
                isVisible = true
                setup(
                    filteringBarColors,
                    filteringBarTextStyle,
                    false,
                    viewLifecycleOwner.lifecycleScope
                )
                setupFavoritesButton(this)
            }

            val filteringBarData = viewModel.getFilteringBarData(
                this@ListFragment,
                data.analytics.screenName
            )
            update(filteringBarData)
        }
    }

    private fun toggleEmptyView(enable: Boolean) {
        data.myFavorites?.emptyPage?.let {
            with(binding) {
                baseListEmptyView.isVisible = enable
                baseListRecyclerView.isVisible = !enable
                baseListEmptyViewImage.setImageFrom(
                    it.image,
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
                baseListEmptyViewTitle.text = localizationService.getString(it.title)
                baseListEmptyViewTitle.setTextColor(emptyViewColors.title)
                baseListEmptyViewTitle.setFont(emptyViewTextStyle.title)
                baseListEmptyViewSubtitle.text =
                    localizationService.getString(it.subtitle)
                baseListEmptyViewSubtitle.setTextColor(emptyViewColors.subtitle)
                baseListEmptyViewSubtitle.setFont(emptyViewTextStyle.subtitle)
            }
        }
    }

    private fun setupFavoritesButton(filteringBar: FilteringBar) {
        data.myFavorites?.let { config ->
            config.filteringButton?.let {
                val defaultState =
                    FilteringBarCell.ButtonState.State(
                        it.unselected.title,
                        it.unselected.icon,
                        it.unselected.accessibilityLabel
                    )
                val selectedState = FilteringBarCell.ButtonState.State(
                    it.selected.title,
                    it.selected.icon,
                    it.selected.accessibilityLabel
                )
                val isCheckedAtSetup =
                    viewModel.getCurrentFilterState().mode == FilteringHandler.Mode.MY_FAVORITES || config.activeOnLoad
                filteringBar.insertButton(
                    isCheckedAtSetup,
                    defaultState,
                    selectedState,
                ) { selected ->
                    toggleFavoriteMode(selected)
                }
            } ?: run {
                toggleFavoriteMode(config.activeOnLoad)
            }
        }
    }

    private fun toggleFavoriteMode(enable: Boolean) {
        val newMode = if (enable) {
            FilteringHandler.Mode.MY_FAVORITES
        } else {
            FilteringHandler.Mode.DEFAULT
        }
        viewModel.switchMode(newMode)
    }

    private fun setupSearchIcon() {
        data.search?.let { dataSearch ->
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search)
            binding.baseListToolbar.insertMenuOption(
                title = null,
                icon = drawable,
                side = KibaToolbar.Side.RIGHT,
                index = 0,
                accessibilityLabel = localizationService.getStringFromRepository(
                    "common.search"
                ),
            ) {
                routeController.resolveRouteLink(dataSearch.onTapRouteLink, this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        App.track(screenAnalytics)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putKiboSerializable(SAVED_FILTERING_KEY, viewModel.getCurrentFilterState())
    }

    private companion object {
        const val SAVED_FILTERING_KEY = "SAVED_FILTERING_KEY"
    }
}

