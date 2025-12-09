package com.greencopper.event.activity.ui.activitydetail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.services.track
import com.greencopper.event.activity.ActivityDetailLayoutData
import com.greencopper.event.activity.ui.*
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.activity.viewmodel.ActivityDetailViewModel
import com.greencopper.event.colors.EventColor
import com.greencopper.event.metrics.activityDetail
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.interfacekit.favorites.translate
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class ActivityDetailFragment : DetailFragment<ActivityDetailLayoutData> {

    constructor(activityDetailData: ActivityDetailLayoutData) : super(activityDetailData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val viewModel: ActivityDetailViewModel by viewModel()
    override val screenName: String
        get() = data.analytics.screenName
    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onScheduleItemTap = data.onScheduleItemTap?.let { return@let this::onScheduleItemTap }
        setupAdapter(data.myScheduleEditingInfo, data.stageDetailIcon, onScheduleItemTap)

        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getActivity(screenName, data.activityId, data.displayableTags, data.hideEndTime)
                .flowOn(Dispatchers.IO)
                .collectLatest { detailViewData ->
                    setupDetailView(detailViewData)
                }
        }
    }

    private fun setupFavoriteIcon(details: DetailViewData<Long>) {
        val favoritesEditing = data.favoritesEditing?.translate(localizationService) ?: return
        setupFavoriteIcon(
            favoritesEditing.translate(localizationService),
            EventColor.activityDetail.header.myActivityIcon,
            viewModel.myActivitiesManager.isInFavorites(data.activityId),
            {
                viewModel.addToFavorite(details)
                App.track(
                    AddToMyActivitiesAnalytics(
                        screenName = screenName,
                        itemId = details.itemId,
                        itemName = details.name
                    )
                )
            },
            {
                viewModel.removeFromFavorite(details)
                App.track(
                    RemoveFromMyActivitiesAnalytics(
                        screenName = screenName,
                        itemId = details.itemId,
                        itemName = details.name
                    )
                )
            }
        )
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            App.track(
                ScreenViewEvent(
                    Screen.activityDetail(screenName),
                    createEventParams()
                )
            )
        }
    }

    private fun setupDetailView(details: DetailViewData<Long>) {
        hideLoading()
        details.widgetCollectionKey?.let {
            setupWidgetCollectionView(it)
        }

        setDetailItem(details)
        setupFavoriteIcon(details)

        setupScheduleItemList(details, data.myScheduleEditingInfo, data.stageDetailIcon, false)
    }

    override fun restoreData(encodedData: String): ActivityDetailLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private suspend fun createEventParams(): Map<EventParameter, String> {
        return mutableMapOf(EventParameter.itemId to data.activityId.toString()).apply {
            viewModel.getActivityDefaultName(data.activityId)?.let {
                put(EventParameter.itemName, it)
            }
        }
    }

    private fun onScheduleItemTap(viewData: ScheduleItemViewData) {
        data.onScheduleItemTap?.let { onScheduleItemTap ->
            linkResolver.route(
                onScheduleItemTap,
                mapOf("scheduleItemId" to viewData.itemId.toString())
            )?.let { route ->
                routeController.redirect(route, this@ActivityDetailFragment)
            }
        }
    }
}
