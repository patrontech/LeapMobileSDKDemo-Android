package com.greencopper.event.scheduleItem.ui.schedule

import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.greencopper.core.data.*
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.event.R
import com.greencopper.event.colors.EventColor
import com.greencopper.event.databinding.ScheduleFragmentBinding
import com.greencopper.event.scheduleItem.ScheduleLayoutData
import com.greencopper.event.scheduleItem.ui.datepicker.DateChangeListener
import com.greencopper.event.scheduleItem.ui.datepicker.DatePickerView
import com.greencopper.event.scheduleItem.ui.timeline.TimelineAdapter
import com.greencopper.event.scheduleItem.ui.timeline.TimelineLayoutManager
import com.greencopper.event.scheduleItem.viewmodel.*
import com.greencopper.event.scheduleItem.viewmodel.ScheduleListViewModel.SavedFiltering
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.favorites.toFavoriteIcons
import com.greencopper.interfacekit.favorites.translate
import com.greencopper.interfacekit.filtering.FilteringHandler.Mode
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.fragment.launchRepeatingJob
import com.greencopper.interfacekit.ui.views.navigationcontrols.KibaToolbar
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

internal class ScheduleListFragment : ParameterizedFragment<ScheduleLayoutData>, RedirectableLayout,
    DateChangeListener {

    constructor(scheduleData: ScheduleLayoutData) : super(scheduleData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val binding: ScheduleFragmentBinding by viewBinding(ScheduleFragmentBinding::inflate)
    override val screenColor: ScreenColor get() = colors
    override val redirectionHash: RedirectionHash get() = data.redirectionHash

    private val localizationService: LocalizationService by App.lazy()

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.scheduleListToolbar,
            colors.topBar,
            EventTextStyle.schedule.topBar,
            localizationService.getString(data.title ?: "event.schedule.title"),
        )

    private var savedFiltering: SavedFiltering? = null

    private val viewModel: ScheduleListViewModel by viewModel {
        val state = savedState ?: ScheduleState()
        listOf(
            if (!state.isInMySchedule) Mode.DEFAULT else Mode.MY_FAVORITES,
            computeFilteringModes(),
            data,
            state
        )
    }

    private val colors by lazy { EventColor.schedule }
    private val listAdapter: ScheduleListAdapter by lazy {
        ScheduleListAdapter(
            viewModel::onScheduleItemTap,
            {
                viewModel.onAddRemoveFavoritesTap(
                    it.isInMySchedule,
                    it.itemId,
                    it.name,
                    it.timeSlot.startDate
                )
            },
            viewModel::onNextDateTap,
            lifecycleScope,
            data.favoritesEditing?.translate(localizationService)?.toFavoriteIcons(),
            data.displayImages,
            this,
            data.analytics.screenName,
            viewModel.conditionChecker,
        ).apply {
            stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }
    private lateinit var timelineAdapter: TimelineAdapter
    private var timelineLayoutManager: TimelineLayoutManager? = null
    private var timelineSavedState: Parcelable? = null

    private fun computeFilteringModes(): Map<Mode, FilteringInfo?> {
        val filteringModes = mutableMapOf<Mode, FilteringInfo?>()

        filteringModes[Mode.DEFAULT] = data.filtering
        filteringModes[Mode.MY_FAVORITES] = data.myFavorites?.filtering

        return filteringModes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedState = savedInstanceState?.getKiboSerializable<ScheduleState>(SAVED_STATE)
        savedFiltering = savedInstanceState?.getKiboSerializable<SavedFiltering>(SAVED_FILTERING_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            timelineSavedState = it.getParcelable(SAVED_TIMELINE_STATE_KEY)
        }

        view.setBackgroundColor(colors.background)
        with(binding) {
            barSeparatorTop.setBackgroundColor(colors.filters.border)
            barSeparatorBottom.setBackgroundColor(colors.filters.border)

            scheduleListDatePicker.setDateChangeListener(this@ScheduleListFragment)
            scheduleEmpty.setup(colors.empty, EventTextStyle.schedule.empty)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val topBar = viewModel.getInitialSetup(this@ScheduleListFragment)
            topBar.buttons.forEachIndexed { index, button ->
                binding.scheduleListToolbar.insertMenuOption(
                    title = button.title,
                    iconName = button.icon,
                    icon = button.iconResource?.let {
                        ContextCompat.getDrawable(requireContext(), it)
                    },
                    side = KibaToolbar.Side.RIGHT,
                    index = index,
                    accessibilityLabel = localizationService.getString(
                        button.accessibilityLabel ?: button.title
                    ),
                ) {
                    viewModel.sendAction(button.onClick)
                }
            }

            setupFilterBar()
            setupList()
            setupTimeline()

            collectDatePicker()
            collectScheduleList()
            collectTimeline()
            collectEmptyState()
            collectSelectedSchedule()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putKiboSerializable(SAVED_FILTERING_KEY, viewModel.getCurrentFilterState())
        timelineLayoutManager?.let {
            val onSaveInstanceState = it.onSaveInstanceState()
            outState.putParcelable(SAVED_TIMELINE_STATE_KEY, onSaveInstanceState)
        }
        viewModel.saveState(outState, SAVED_STATE)
    }

    override fun onDestroyView() {
        timelineSavedState = timelineLayoutManager?.onSaveInstanceState()
        super.onDestroyView()
    }

    override fun onDateChanged(pickedDate: ZonedDateTime) {
        viewModel.onDatePickerDateTap(pickedDate)
    }

    private var savedState: ScheduleState? = null

    private fun setupFilterBar() {
        val filteringBarButtons = viewModel.getFilteringBarButtons()
        val filteringBarData = viewModel.getFilteringBarData(this)

        if (filteringBarButtons.isEmpty() && filteringBarData.filters.isEmpty()) {
            binding.scheduleListFilteringBar.isVisible = false
            return
        }

        binding.scheduleListFilteringBar.setup(
            EventColor.schedule.filters,
            EventTextStyle.schedule.header.filters,
            false,
            viewLifecycleOwner.lifecycleScope
        )

        binding.scheduleListFilteringBar.update(viewModel.getFilteringBarData(this@ScheduleListFragment))

        filteringBarButtons.forEach { buttonState ->
            binding.scheduleListFilteringBar.insertButton(buttonState)
        }

        binding.scheduleListFilteringBar.isVisible = true
        binding.barSeparatorBottom.isVisible = true
        binding.barSeparatorTop.isVisible = true

        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.filteringUpdater.collectLatest {
                binding.scheduleListFilteringBar.update(viewModel.getFilteringBarData(this@ScheduleListFragment))
            }
        }
    }

    private fun setupList() {
        with(binding.scheduleListRecycler) {
            adapter = listAdapter
            itemAnimator = null
            val horizontalMargin = resources.getDimension(R.dimen.horizontal_margin).toInt()
            val dimen24 = 24.dpToPx()
            val dimen16 = 16.dpToPx()
            val dimen8 = 8.dpToPx()
            addItemDecoration(
                MappedDynamicVerticalSpacingItemDecorator(
                    mapOf(
                        ScheduleListAdapter.VIEW_TYPE_TIME_HEADER to Rect(0, dimen16, 0, dimen8),
                        ScheduleListAdapter.VIEW_TYPE_DAY_HEADER to Rect(
                            horizontalMargin, dimen24, horizontalMargin, dimen16
                        ),
                        ScheduleListAdapter.VIEW_TYPE_CARD to Rect(horizontalMargin, dimen8, horizontalMargin, dimen8),
                        ScheduleListAdapter.VIEW_TYPE_WIDGETS to Rect(0, 0, 0, 0),
                        ScheduleListAdapter.VIEW_TYPE_NEXT_DATE to Rect(
                            horizontalMargin,
                            dimen8,
                            horizontalMargin,
                            dimen16
                        ),
                    ),
                    false
                )
            )
        }
    }

    private fun collectDatePicker() {
        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.datePickerState.collectLatest { state ->
                state?.takeIf { state.dates.isNotEmpty() }?.let {
                    binding.scheduleListDatePicker.setDates(
                        it.dates,
                        when (it.displayMode) {
                            DisplayMode.DAILY -> DatePickerView.DisplayMode.DAILY
                            else -> DatePickerView.DisplayMode.MONTHLY
                        },
                        it.selectedDate
                    )

                    binding.scheduleListDatePicker.isVisible = true
                } ?: run {
                    binding.scheduleListDatePicker.isVisible = false
                }
            }
        }
    }

    private fun collectScheduleList() {
        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.listState.collectLatest {
                it?.let { content ->
                    binding.scheduleListRecycler.isInvisible = false

                    listAdapter.setViewData(content.items) {
                        val nextScheduledItemPos = viewModel.findNextScheduledItemPosition(content.items)
                        binding.scheduleListRecycler.scrollToPosition(nextScheduledItemPos)
                    }
                } ?: run {
                    binding.scheduleListRecycler.isInvisible = true
                }
            }
        }
    }

    private fun collectTimeline() {
        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.timelineState.collectLatest {
                it?.let { content ->
                    binding.scheduleTimeline.isInvisible = false

                    timelineAdapter.setEvents(content.items)
                } ?: run {
                    binding.scheduleTimeline.isInvisible = true
                }
            }
        }
    }

    private fun collectEmptyState() {
        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.emptyState.collectLatest {
                it?.let { emptyContent ->
                    binding.scheduleEmpty.isInvisible = false
                    binding.scheduleEmpty.fillIn(
                        emptyState = emptyContent,
                        origin = this@ScheduleListFragment,
                        conditionChecker = viewModel.conditionChecker
                    )
                } ?: run {
                    binding.scheduleEmpty.isInvisible = true
                }
            }
        }
    }

    private fun collectSelectedSchedule() {
        viewLifecycleOwner.launchRepeatingJob(Lifecycle.State.STARTED) {
            viewModel.selectedSchedule.collectLatest {
                val nextScheduledItemPos = viewModel.findNextScheduledItemPosition(viewModel.listState.value?.items.orEmpty())
                binding.scheduleListRecycler.scrollToPosition(nextScheduledItemPos)
            }
        }
    }

    private fun setupTimeline() {
        data.timeline?.let { timeline ->
            with(binding) {
                with(scheduleTimeline) {
                    timelineAdapter = TimelineAdapter(this@ScheduleListFragment)
                    timelineLayoutManager = TimelineLayoutManager(timelineAdapter) {
                        timelineLayoutManager?.onRestoreInstanceState(timelineSavedState)
                    }
                    timelineAdapter.setup(
                        timeline.preferredTimeToWidthRatio,
                        data.favoritesEditing?.translate(localizationService),
                        { selectedItem ->
                            viewModel.onScheduleItemTap(selectedItem.id)
                        },
                        { selectedItem ->
                            viewModel.onAddRemoveFavoritesTap(
                                selectedItem.isInMySchedule,
                                selectedItem.id,
                                selectedItem.name,
                                selectedItem.startDate
                            )
                        },
                        viewModel.timezoneProvider,
                        viewLifecycleOwner.lifecycleScope
                    )
                    layoutManager = timelineLayoutManager
                    adapter = timelineAdapter
                    setItemViewCacheSize(10)
                    setHasFixedSize(true)
                }
            }
        }
    }

    private companion object {
        const val SAVED_FILTERING_KEY = "SAVED_FILTERING_KEY"
        const val SAVED_TIMELINE_STATE_KEY = "SAVED_TIMELINE_STATE_KEY"
        const val SAVED_STATE = "SAVED_STATE"
    }

    override fun restoreData(encodedData: String): ScheduleLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
