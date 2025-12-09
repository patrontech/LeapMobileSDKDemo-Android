package com.greencopper.event.performers.ui.performerdetail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.services.track
import com.greencopper.event.activity.ui.*
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.colors.EventColor
import com.greencopper.event.metrics.performerDetail
import com.greencopper.event.performers.PerformerDetailLayoutData
import com.greencopper.event.performers.ui.AddToMyPerformersAnalytics
import com.greencopper.event.performers.ui.RemoveFromMyPerformersAnalytics
import com.greencopper.event.performers.viewmodel.PerformerDetailViewModel
import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.interfacekit.favorites.translate
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

internal class PerformerDetailFragment : DetailFragment<PerformerDetailLayoutData> {
    constructor(performerDetailData: PerformerDetailLayoutData) : super(performerDetailData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val viewModel: PerformerDetailViewModel by viewModel()
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
            viewModel.getPerformer(screenName, data.performerId, data.displayableTags, data.hideEndTime)
                .flowOn(Dispatchers.IO)
                .collectLatest { detailViewData ->
                    setupDetailView(detailViewData)
                }
        }
    }

    private fun setupFavoriteIcon(details: DetailViewData<String>) {
        val favoritesEditing = data.favoritesEditing?.translate(localizationService) ?: return
        setupFavoriteIcon(
            favoritesEditing,
            EventColor.activityDetail.header.myActivityIcon,
            viewModel.myPerformersManager.isInFavorites(data.performerId),
            {
                viewModel.addToFavorite(details)
                App.track(
                    AddToMyPerformersAnalytics(
                        screenName = screenName,
                        itemId = details.itemId,
                        itemName = details.name
                    )
                )
            },
            {
                viewModel.removeFromFavorite(details)
                App.track(
                    RemoveFromMyPerformersAnalytics(
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
                    Screen.performerDetail(screenName),
                    createEventParams()
                )
            )
        }
    }

    private fun setupDetailView(details: DetailViewData<String>) {
        hideLoading()
        details.widgetCollectionKey?.let {
            setupWidgetCollectionView(it)
        }

        setDetailItem(details, showScheduleInCard = true)
        setupFavoriteIcon(details)

        setupScheduleItemList(details, data.myScheduleEditingInfo, data.stageDetailIcon, showTitle = true,  showInCard = true)
    }

    override fun restoreData(encodedData: String): PerformerDetailLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private suspend fun createEventParams(): Map<EventParameter, String> {
        return mutableMapOf(EventParameter.itemId to data.performerId).apply {
            viewModel.getPerformerDefaultName(data.performerId)?.let {
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
                routeController.redirect(route, this@PerformerDetailFragment)
            }
        }
    }
}
