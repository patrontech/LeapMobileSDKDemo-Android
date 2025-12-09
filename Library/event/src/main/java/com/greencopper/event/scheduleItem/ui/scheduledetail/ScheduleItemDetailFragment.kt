package com.greencopper.event.scheduleItem.ui.scheduledetail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.services.track
import com.greencopper.event.activity.ui.DetailFragment
import com.greencopper.event.activity.ui.viewdata.DetailViewData
import com.greencopper.event.metrics.scheduleItemDetail
import com.greencopper.event.scheduleItem.ui.bind
import com.greencopper.event.scheduleItem.viewmodel.ScheduleItemDetailViewModel
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import kotlinx.coroutines.launch

internal class ScheduleItemDetailFragment : DetailFragment<ScheduleItemDetailLayoutData>,
    RedirectableLayout {

    constructor(scheduleItemDetailData: ScheduleItemDetailLayoutData) : super(scheduleItemDetailData)

    @Deprecated("For system purpose only. Don't use it")
    constructor() : super(null)

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override val viewModel: ScheduleItemDetailViewModel by viewModel()
    override val screenName: String
        get() = data.analytics.screenName

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel
                .getScheduleDetailItem(screenName, data.scheduleItemId, data.displayableTags, data.hideEndTime)
                .collect { scheduleDetailItem ->
                    setupDetailView(scheduleDetailItem)
                }
        }

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            App.track(
                ScreenViewEvent(
                    Screen.scheduleItemDetail(screenName),
                    createEventParams()
                )
            )
        }
    }

    private fun setupDetailView(details: DetailViewData<Long>) {
        hideLoading()
        setDetailItem(details)

        // Always single scheduleItem for this detail page
        binding.detailScheduleItemView.bind(
            details.scheduleItemList[0],
            this,
            lifecycleScope,
            data.myScheduleEditingInfo,
            data.stageDetailIcon,
        )
        details.widgetCollectionKey?.let {
            setupWidgetCollectionView(it)
        }
    }

    private suspend fun createEventParams(): Map<EventParameter, String> {
        return mutableMapOf(EventParameter.itemId to data.scheduleItemId.toString()).apply {
            viewModel.getScheduleItemDefaultName(data.scheduleItemId)?.let {
                put(EventParameter.itemName, it)
            }
        }
    }

    override fun restoreData(encodedData: String): ScheduleItemDetailLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}
