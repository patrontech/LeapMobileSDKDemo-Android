package com.greencopper.event.activity.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.event.activity.ui.activitydetail.ScheduleItemCardListAdapter
import com.greencopper.event.activity.ui.utils.DescriptionState
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.colors.EventColor
import com.greencopper.event.common.DetailViewModel
import com.greencopper.event.databinding.DetailFragmentBinding
import com.greencopper.event.scheduleItem.data.MyScheduleEditingInfo
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.event.scheduleItem.ui.bind
import com.greencopper.event.textstyle.EventTextStyle
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.favorites.FavoritesEditing
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal abstract class DetailFragment<T : KiboSerializable<T>>(detailData: T?) :
    ParameterizedFragment<T>(detailData), RedirectableLayout {

    protected val localizationService: LocalizationService by lazy { App.resolve() }
    protected val routeController: RouteController by App.lazy()
    protected val linkResolver: LinkResolver by App.lazy()
    protected lateinit var scheduleAdapter: ScheduleItemCardListAdapter

    override val screenColor: ScreenColor get() = EventColor.activityDetail
    override val binding: DetailFragmentBinding by viewBinding(DetailFragmentBinding::inflate)
    private val descriptionMaxLines = 10
    private lateinit var layoutManager: DisableScrollingLayoutManager

    abstract val viewModel: DetailViewModel<*>
    abstract val screenName: String

    override fun createNavigationControlsHandler(): NavigationControlsHandler? =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            EventColor.activityDetail.topBar,
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDescriptionState(DescriptionState.NoOverflow)
        binding.detailDescriptionTextView.apply {
            maxLines = descriptionMaxLines

            doAfterTextChanged {
                post {
                    if (this.lineCount > descriptionMaxLines) {
                        setDescriptionState(DescriptionState.Contracted)

                        binding.detailShowMoreTextView.setOnSafeClickListener(500) {
                            expandButtonClicked()
                        }
                    } else {
                        setDescriptionState(DescriptionState.NoOverflow)
                    }
                }
            }
        }
        binding.detailDescriptionTitleTextView.text =
            localizationService.getString("event.activity.detail.description_title")
        binding.detailScheduleItemsTitleTextView.text =
            localizationService.getString("event.activity.detail.schedule_items_title")
        setDetailStyles()
        setSingleScheduleItemStyles()

        layoutManager = DisableScrollingLayoutManager(requireContext())
        binding.detailScheduleRecyclerView.layoutManager = layoutManager
    }

    protected fun setupAdapter(
        myScheduleEditingInfo: MyScheduleEditingInfo?,
        stageDetailIcon: String,
        onScheduleItemTap: ((ScheduleItemViewData) -> Unit)?,
    ) {
        scheduleAdapter = ScheduleItemCardListAdapter(
            requireContext(),
            viewLifecycleOwner.lifecycleScope,
            this,
            myScheduleEditingInfo,
            stageDetailIcon,
            onScheduleItemTap,
        )

        binding.detailScheduleRecyclerView.apply {
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL) {
                (layoutManager as? LinearLayoutManager)?.stackFromEnd = true
            }

            PagerSnapHelper().attachToRecyclerView(this)
            adapter = scheduleAdapter
            itemAnimator = null
            addItemDecoration(HorizontalSpacingItemDecorator(spacing = 8.dpToPx()))
        }
    }

    protected fun setupScheduleItemList(
        details: DetailViewData<*>,
        myScheduleEditingInfo: MyScheduleEditingInfo? = null,
        stageDetailIcon: String,
        showTitle: Boolean,
        showInCard: Boolean = false,
    ) {
        with(details.scheduleItemList) {
            when {
                !showInCard && size == 1 -> {
                    val scheduleItem = details.scheduleItemList[0]
                    binding.detailScheduleItemView.bind(
                        scheduleItem = scheduleItem,
                        origin = this@DetailFragment,
                        lifecycleScope = lifecycleScope,
                        myScheduleEditingInfo = myScheduleEditingInfo,
                        stageDetailIcon = stageDetailIcon,
                        isSingleItem = true
                    )
                }

                showInCard && size == 1 -> {
                    scheduleAdapter.setScheduleItems(this, showTitle)
                }

                isNotEmpty() && haveSameStage(this) -> {
                    val scheduleItemListWithoutStage = this.map { it.copy(stage = null) }
                    scheduleAdapter.setScheduleItems(scheduleItemListWithoutStage, showTitle)
                    first().stage?.let { stage ->
                        binding.detailStageView.stageTv.text = stage
                        binding.detailStageView.stageMapPin
                            .setImageFrom(
                                stageDetailIcon,
                                viewLifecycleOwner.lifecycleScope,
                                hideIfUnknown = true,
                                hideIfLoading = true,
                            )
                        binding.detailStageView.stageMapPin.setOnSafeClickListener {
                            first().stageDetailLink
                                ?.let { linkResolver.route(it) }
                                ?.let { routeController.resolve(it, this@DetailFragment) }
                        }
                        binding.detailStageView.root.isVisible = true
                    }
                }

                else -> scheduleAdapter.setScheduleItems(this, showTitle)
            }

            layoutManager.canScroll = scheduleAdapter.itemCount != 1
        }
    }

    private fun haveSameStage(scheduleItems: List<ScheduleItemViewData>): Boolean {
        return scheduleItems.map { it.stage }.distinct().size == 1
    }

    protected fun showLoading() {
        binding.detailScrollView.visibility = View.GONE
        binding.detailProgressBar.visibility = View.VISIBLE
    }

    protected fun hideLoading() {
        binding.detailScrollView.visibility = View.VISIBLE
        binding.detailProgressBar.visibility = View.GONE
    }

    protected fun setupWidgetCollectionView(primaryZoneWidgetKey: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.detailWidgetCollectionView.bind(
                    widgetItems = viewModel.getWidgetItems(primaryZoneWidgetKey),
                    origin = this@DetailFragment,
                    screenName = screenName,
                    topMarginOverride = 16.dpToPx(),
                    bottomMarginOverride = 0,
                ).collect()
            }
        }
    }

    protected fun setDetailItem(details: DetailViewData<*>, showScheduleInCard: Boolean = false) {
        with(binding) {
            details.photo?.let {
                detailHeaderView.setImageFrom(
                    it,
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true
                ) { imageResult ->
                    imageResult.drawable?.also { safeDrawable ->
                        val ratio = safeDrawable.intrinsicWidth.toFloat() / safeDrawable.intrinsicHeight.toFloat()
                        if (ratio > 1.7) {
                            detailHeaderView.updateLayoutParams {
                                height = 200.dpToPx()
                            }
                        } else {
                            detailHeaderView.minimumHeight = 200.dpToPx()
                            detailHeaderView.maxHeight = 400.dpToPx()
                        }
                    }
                }

            } ?: run {
                detailHeaderView.visibility = View.GONE
                detailHeaderGoneSpacing.visibility = View.VISIBLE
            }

            detailTitleTextView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    detailTitleTextView.setOverFlow(minTextSize = 22, maxTextSize = 26)
                    detailTitleTextView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
            detailTitleTextView.text = localizationService.getString(details.name)

            detailSubtitleTextView.setOtaTextOrGone(localizationService, details.subtitle)
            detailDescriptionTitleTextView.isVisible = !details.description.isNullOrEmpty()
            detailDescriptionTextView.isVisible = !details.description.isNullOrEmpty()
            detailDescriptionTextView.text = details.description

            detailScheduleItemView.root.isVisible = !showScheduleInCard && details.scheduleItemList.size == 1
            detailScheduleItemsTitleTextView.isVisible = details.scheduleItemList.size > 1
            detailScheduleRecyclerView.isVisible = showScheduleInCard || details.scheduleItemList.size > 1

            detailTagDisplay.isVisible = details.tags.isNotEmpty()
            detailTagDisplay.setTags(details.tags)
        }
    }

    protected fun setupFavoriteIcon(
        favoritesEditing: FavoritesEditing,
        color: Int,
        isInFavorites: Boolean,
        onAddToFavorites: () -> Unit,
        onRemoveFromFavorites: () -> Unit,
    ) {
        with(binding.detailFavoriteAddRemove) {
            isVisible = true
            setColorFilter(color)
            if (isInFavorites) {
                contentDescription = localizationService.getString(favoritesEditing.remove.accessibilityLabel)
                setImageFrom(
                    favoritesEditing.remove.icon,
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
                setOnSafeClickListener { onRemoveFromFavorites() }
            } else {
                contentDescription = localizationService.getString(favoritesEditing.add.accessibilityLabel)
                setImageFrom(
                    favoritesEditing.add.icon,
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
                setOnSafeClickListener { onAddToFavorites() }
            }
        }
    }

    private fun setDetailStyles() {
        val colors = EventColor.activityDetail
        val textStyles = EventTextStyle.activityDetail

        with(binding) {
            detailScrollView.setBackgroundColor(colors.background)

            detailTitleTextView.setTextColor(colors.header.title)
            detailTitleTextView.setFont(textStyles.header.title)

            detailSubtitleTextView.setTextColor(colors.header.subtitle)
            detailSubtitleTextView.setFont(textStyles.header.subtitle)

            detailStageView.stageTv.setTextColor(colors.mainSchedule.stage.name)
            detailStageView.stageTv.setFont(textStyles.mainSchedule.stage)

            detailScheduleItemsTitleTextView.setTextColor(colors.upcomingTimes.title)
            detailScheduleItemsTitleTextView.setFont(textStyles.upcomingTimes.title)

            detailDescriptionTitleTextView.setTextColor(colors.description.title)
            detailDescriptionTitleTextView.setFont(textStyles.description.title)

            with(detailDescriptionTextView) {
                setTextColor(colors.description.text)
                setFont(textStyles.description.text)
                movementMethod = ClickableLinkMovementMethod()
            }

            detailShowMoreTextView.setTextColor(colors.description.showMore)
            detailShowMoreTextView.setFont(textStyles.description.showMore)

            detailStageView.stageIv.setColorFilter(colors.mainSchedule.stage.name)
            detailStageView.stageMapPin.setColorFilter(colors.mainSchedule.stage.mapPin)
            detailShowMoreIcon.setColorFilter(colors.description.showMore)

            detailTagDisplay.setup(colors.tags)
        }
    }

    private fun setSingleScheduleItemStyles() {
        val colors = EventColor.activityDetail.mainSchedule
        val textStyles = EventTextStyle.activityDetail.mainSchedule

        with(binding.detailScheduleItemView) {
            scheduleItemTvDayOfEvent.setTextColor(colors.date.day)
            scheduleItemTvDayOfEvent.setFont(textStyles.day)

            scheduleItemTvTimeOfEvent.setTextColor(colors.date.hours)
            scheduleItemTvTimeOfEvent.setFont(textStyles.hours)

            scheduleItemStage.stageTv.setTextColor(colors.stage.name)
            scheduleItemStage.stageTv.setFont(textStyles.stage)

            scheduleItemIv.setColorFilter(colors.date.icon)
            scheduleItemStage.stageIv.setColorFilter(colors.stage.icon)
            scheduleItemStage.stageMapPin.setColorFilter(colors.stage.mapPin)
            scheduleItemAddRemove.setColorFilter(colors.mySchedule.selected)
        }
    }

    private fun expandButtonClicked() {
        val maxLines = binding.detailDescriptionTextView.maxLines
        val lineCount = binding.detailDescriptionTextView.lineCount
        if (maxLines == descriptionMaxLines) {
            val animation =
                ObjectAnimator.ofInt(
                    binding.detailDescriptionTextView,
                    "maxLines",
                    maxLines,
                    lineCount
                )
            animation.setDuration(100).start()
            setDescriptionState(DescriptionState.Expanded)
        } else {
            val animation =
                ObjectAnimator.ofInt(
                    binding.detailDescriptionTextView,
                    "maxLines",
                    maxLines,
                    descriptionMaxLines
                )
            animation.setDuration(100).start()
            setDescriptionState(DescriptionState.Contracted)
        }
    }

    private fun setDescriptionState(state: DescriptionState) {
        if (view != null) {
            binding.detailShowMoreGroup.isVisible = state.activated
            binding.detailShowMoreIcon.setImageResource(state.arrowDrawableId)
            binding.detailShowMoreGradientView.foreground = state.foregroundDrawable
            state.textId?.let { binding.detailShowMoreTextView.text = localizationService.getString(it) }
        }
    }
}

private class DisableScrollingLayoutManager(context: Context) : LinearLayoutManager(context) {

    init {
        orientation = RecyclerView.HORIZONTAL
    }

    var canScroll = true

    override fun canScrollVertically(): Boolean {
        return canScroll && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return canScroll && super.canScrollHorizontally()
    }
}
