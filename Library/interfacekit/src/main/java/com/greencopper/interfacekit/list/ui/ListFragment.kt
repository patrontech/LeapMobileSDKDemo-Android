package com.greencopper.interfacekit.list.ui

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.getKiboSerializable
import com.greencopper.core.data.putKiboSerializable
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.ListFragmentBinding
import com.greencopper.interfacekit.empty.ui.EmptyView
import com.greencopper.interfacekit.filtering.FilteringHandler
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.list.initializer.ListLayoutData
import com.greencopper.interfacekit.list.viewmodel.*
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.fragment.launchRepeatingJob
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import kotlinx.coroutines.flow.collectLatest

internal class ListFragment : ParameterizedFragment<ListLayoutData>, RedirectableLayout {

    constructor(listData: ListLayoutData) : super(listData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    private var savedState: ListState? = null
    private var savedFiltering: ListViewModel.SavedFiltering? = null

    private val uiClient by lazy {
        ListReducer.UiClient {
            gridState.requestScrollToItem(0)
        }
    }

    private val gridState by lazy { LazyGridState(0, 0) }

    private val viewModel: ListViewModel by viewModel {
        val state = savedState ?: ListState(
            selectedList = mutableSetOf<SelectedList>().apply {
                if (data.myFavorites?.activeOnLoad == true) {
                    add(SelectedList.MyFavorites)
                }
                if (data.myInterests?.activeOnLoad == true) {
                    add(SelectedList.MyInterests)
                }
            }
        )
        listOf(
            if (state.isInMyFavorites) FilteringHandler.Mode.MY_FAVORITES else FilteringHandler.Mode.DEFAULT,
            savedFiltering?.filteringInfoMap ?: computeFilteringModes(),
            data,
            state,
        )
    }
    private val colors by lazy { InterfaceKitColor.list }
    private val textStyles by lazy { InterfaceKitTextStyle.list }
    override val screenColor: ScreenColor get() = colors
    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.listToolbar,
            colors.topBar,
            textStyles.topBar,
            viewModel.viewBuilder.localizationService.getString(data.topBar.title ?: ""),
        )

    override val binding: ListFragmentBinding by viewBinding(ListFragmentBinding::inflate)

    private fun computeFilteringModes(): Map<FilteringHandler.Mode, FilteringInfo?> {
        val filteringModes = mutableMapOf<FilteringHandler.Mode, FilteringInfo?>()

        filteringModes[FilteringHandler.Mode.DEFAULT] = data.filtering
        filteringModes[FilteringHandler.Mode.MY_FAVORITES] = data.myFavorites?.filtering

        return filteringModes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedState = savedInstanceState?.getKiboSerializable<ListState>(SAVED_STATE)
        savedFiltering = savedInstanceState?.getKiboSerializable<ListViewModel.SavedFiltering>(SAVED_FILTERING_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setBackgroundColor(colors.background)

        viewModel.initialSetup(this, uiClient)

        binding.listComposeContainer.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                viewModel.viewBuilder.buildContent {
                    ComposeContainer(data)
                }
            }
        }

        binding.listToolbar.setupTopBarData(data.topBar, this)

        setupFilterBar()
    }

    private fun setupFilterBar() {
        val filteringBarButtons = viewModel.getFilteringBarButtons()
        val filteringBarData = viewModel.getFilteringBarData(this)
        val filteringBar = binding.listFilteringBar

        if (filteringBarButtons.isEmpty() && filteringBarData.filters.isEmpty()) {
            filteringBar.isVisible = false
            return
        }

        val filteringBarColors = InterfaceKitColor.list.filters
        filteringBar.setup(
            filteringBarColors,
            InterfaceKitTextStyle.list.filters,
            false,
            viewLifecycleOwner.lifecycleScope
        )

        filteringBar.update(viewModel.getFilteringBarData(this@ListFragment))

        filteringBarButtons.forEach { buttonState ->
            filteringBar.insertButton(buttonState)
        }

        filteringBar.isVisible = true
        with(binding.listFilteringBarBorderTop) {
            isVisible = true
            setBackgroundColor(filteringBarColors.border)
        }
        with(binding.listFilteringBarBorderBottom) {
            isVisible = true
            setBackgroundColor(filteringBarColors.border)
        }

        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.filteringUpdater.collectLatest {
                filteringBar.update(viewModel.getFilteringBarData(this@ListFragment))
            }
        }
    }

    @Composable
    private fun ComposeContainer(data: ListLayoutData) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val content by viewModel.contentState.collectAsStateWithLifecycle(null)

            when (content) {
                is ViewState.ContentState.Content -> {
                    Content(data, (content as ViewState.ContentState.Content))
                }

                is ViewState.ContentState.Empty -> {
                    Empty(content as ViewState.ContentState.Empty)
                }
            }
        }
    }

    @Composable
    private fun Content(data: ListLayoutData, content: ViewState.ContentState.Content) {
        ListContent(
            mode = data.mode,
            listItems = content.items,
            gridState = gridState,
            onCardTap = { viewModel.onListItemTap(it) },
            onFavoritesTap = { viewModel.sendAction(it.onFavoriteTapAction) },
        )
    }

    @Composable
    private fun Empty(
        content: ViewState.ContentState.Empty,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                EmptyView(context).apply {
                    setup(
                        colors.empty,
                        textStyles.empty,
                    )
                }
            },
            update = { view ->
                view.fillIn(
                    content,
                    this,
                    viewModel.conditionChecker,
                )
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putKiboSerializable(SAVED_FILTERING_KEY, viewModel.getCurrentFilterState())
        viewModel.saveState(outState, SAVED_STATE)
    }

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun restoreData(encodedData: String): ListLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private companion object {
        const val SAVED_FILTERING_KEY = "SAVED_FILTERING_KEY"
        const val SAVED_STATE = "SAVED_STATE"
    }
}
